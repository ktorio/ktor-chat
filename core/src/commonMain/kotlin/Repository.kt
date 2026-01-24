package io.ktor.chat

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.AbstractEncoder
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.serializer
import kotlin.reflect.*

interface Repository<E : Identifiable<ID>, ID> : ReadOnlyRepository<E, ID> {
    suspend fun create(e: E): E
    suspend fun update(e: E)
    suspend fun delete(id: ID)
}

interface ReadOnlyRepository<out E : Identifiable<ID>, ID> {
    suspend fun get(id: ID): E?
    suspend fun list(query: Query = Everything): List<E>
}

sealed interface Query

class MapQuery private constructor(private val map: Map<String, List<Any>>) : Query,
    Map<String, List<Any>> by map {
    companion object {
        fun of(map: Map<String, List<Any>>) =
            if (map.isEmpty()) Everything else MapQuery(map)

        operator fun invoke(builder: Builder.() -> Unit) =
            MapQuery(Builder().apply(builder).build())
    }

    class Builder {
        private val map = mutableMapOf<String, List<Any>>()

        operator fun set(key: String, value: List<Any>) {
            map[key] = value.map { it.toString() }
        }

        operator fun set(key: String, value: Any) {
            map[key] = listOf(value.toString())
        }

        fun build() = MapQuery(map)
    }

    override fun toString(): String =
        map.toString()
}

data object Everything : Query
data object Nothing : Query

inline fun <reified E : Identifiable<ULong>> ListRepository(
    vararg items: E,
    noinline copy: (E, ULong) -> E
): ListRepository<E, ULong> {
    val eType = E::class

    return ListRepository(
        list = items.mapIndexed { index, e -> copy(e, index.toULong() + 1u) }.toMutableList(),
        eType = eType,
        currentId = items.size.toULong(),
        nextId = { it + 1u },
        setId = copy
    )
}

/**
 * In-memory implementation for repository, used for testing.
 */
class ListRepository<E : Identifiable<ID>, ID>(
    private val list: MutableList<E> = mutableListOf(),
    private val eType: KClass<E>,
    private var currentId: ID,
    private val nextId: (ID) -> ID,
    private val setId: (E, ID) -> E,
) : Repository<E, ID> {
    override suspend fun get(id: ID): E? =
        list.find { it.id == id }

    override suspend fun create(e: E): E =
        setId(e, nextId(currentId)).also {
            list.add(it)
        }

    override suspend fun update(e: E) {
        val index = findIndex(e.id).takeIf { it >= 0 } ?: return
        list[index] = e
    }

    override suspend fun delete(id: ID) {
        val index = findIndex(id)
        list.removeAt(index)
    }

    private fun findIndex(id: ID): Int =
        list.indexOfFirst {
            it.id == id
        }

    override suspend fun list(query: Query): List<E> =
        list.filter(query.toPredicate(eType))
}

@OptIn(InternalSerializationApi::class)
fun <E : Any> Query.toPredicate(eType: KClass<E>): (E) -> Boolean =
    when (this) {
        is Everything -> {
            { true }
        }

        is MapQuery -> {
            // Use serialization to match the property of the actual class
            val serializer = eType.serializer()

            val clauses: List<(E) -> Boolean> = entries.map { (key, values) ->
                val propertyExtractor = PropertyExtractor<E, Any?>(key)
                ({ entity: E ->
                    val propertyValue = propertyExtractor.extract(entity, serializer)
                    values.any { value ->
                        propertyValue.toString() == value.toString()
                    }
                })
            }

            // Return a function that checks all clauses
            ({ clauses.all { clause -> clause(it) } })
        }

        else -> { // Nothing
            { false }
        }
    }

suspend fun <E : Identifiable<ID>, ID> Repository<E, ID>.list(params: (MapQuery.Builder) -> Unit): List<E> =
    list(MapQuery.Builder().also(params).build())


// A property extractor that uses serialization to extract a property value
class PropertyExtractor<T, R>(private val propertyName: String) {
    @OptIn(ExperimentalSerializationApi::class)
    fun extract(entity: T, serializer: KSerializer<T>): R {
        var result: Any? = null

        // Create a custom encoder that captures only the specified property
        val encoder = object : AbstractEncoder() {
            override val serializersModule = SerializersModule {}

            override fun encodeElement(descriptor: SerialDescriptor, index: Int): Boolean {
                return descriptor.getElementName(index) == propertyName
            }

            // Implement other encoding methods to capture primitive values
            override fun encodeString(value: String) {
                if (result == null) result = value
            }

            override fun encodeInt(value: Int) {
                if (result == null) result = value
            }

            override fun encodeLong(value: Long) {
                if (result == null) result = value
            }

            override fun encodeDouble(value: Double) {
                if (result == null) result = value
            }

            override fun encodeBoolean(value: Boolean) {
                if (result == null) result = value
            }

            // Add more primitive type handlers as needed
        }

        // Serialize the entity to extract the property
        serializer.serialize(encoder, entity)

        @Suppress("UNCHECKED_CAST")
        return result as R
    }
}
