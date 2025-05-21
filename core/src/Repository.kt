package io.ktor.chat

import kotlinx.datetime.Instant
import kotlin.reflect.*
import kotlin.reflect.full.*

interface Repository<E: Identifiable<ID>, ID> : ReadOnlyRepository<E, ID> {
    suspend fun create(e: E): E
    suspend fun update(e: E)
    suspend fun delete(id: ID)
}

interface ReadOnlyRepository<out E: Identifiable<ID>, ID> {
    suspend fun get(id: ID): E?
    suspend fun list(query: Query = Everything): List<E>
}

sealed interface Query

class MapQuery private constructor(private val map: Map<String, List<Any>>): Query, Map<String, List<Any>> by map {
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
data object Everything: Query
data object Nothing: Query

inline fun <reified E: Identifiable<Long>> ListRepository(vararg items: E): ListRepository<E, Long> {
    val eType = E::class
    val idProperty = eType.memberProperties.find { it.name == "id" }
    check(idProperty != null) {
        "Entity type should have id property"
    }

    val copyFunction = eType.memberFunctions.find { it.name == "copy" }
    check(copyFunction != null) {
        "Entity type should be data class; missing copy() function"
    }

    val instanceParameter = copyFunction.instanceParameter
    val idParameter = copyFunction.parameters.find { it.name == "id" }
    check(instanceParameter != null && idParameter != null) {
        "Expected id parameter to be in copy()"
    }

    val copyWithNewId: (E, Long) -> E = { e, id ->
        eType.cast(copyFunction.callBy(
            mapOf(
                instanceParameter to e,
                idParameter to id,
            )
        ))
    }
    return ListRepository(
        list = items.mapIndexed { index, e -> copyWithNewId(e, index.toLong() + 1L) }.toMutableList(),
        eType = eType,
        currentId = items.size.toLong(),
        nextId = { it + 1L },
        setId = copyWithNewId
    )
}

/**
 * In-memory implementation for repository, used for testing.
 */
class ListRepository<E: Identifiable<ID>, ID>(
    private val list: MutableList<E> = mutableListOf(),
    private val eType: KClass<E>,
    private var currentId: ID,
    private val nextId: (ID) -> ID,
    private val setId: (E, ID) -> E,
): Repository<E, ID> {
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

fun <E: Any> Query.toPredicate(eType: KClass<E>): (E) -> Boolean =
    when (this) {
        is Everything -> {{ true }}
        is Nothing -> {{ false }}

        is MapQuery -> {
            val clauses: List<(E) -> Boolean> = entries.map { (key, values) ->
                val property = findMember(eType, key)
                val getter = property::get
                val parseFunction = property.parseFunction()
                val clause: (E) -> Boolean = { getter(it) in values.map { parseFunction(it.toString()) } }
                clause
            }
            ({ clauses.all { clause -> clause(it) } })
        }
    }

private fun <E : Any> findMember(eType: KClass<E>, key: String): KProperty1<E, *> =
    (eType.memberProperties.find { it.name == key }
        ?: throw IllegalArgumentException("Missing property: $key"))

@Suppress("UNCHECKED_CAST")
private fun <V> KProperty<V>.parseFunction(): (String) -> V {
    when(returnType) {
        String::class.createType() -> return { it as V }
        Int::class.createType() -> return { it.toInt() as V }
        Long::class.createType() -> return { it.toLong() as V }
        Instant::class.createType() -> return { Instant.parse(it) as V }
        else -> throw IllegalArgumentException("Unsupported type: $returnType")
    }
}

suspend fun <E: Identifiable<ID>, ID> Repository<E, ID>.list(params: (MapQuery.Builder) -> Unit): List<E> =
    list(MapQuery.Builder().also(params).build())