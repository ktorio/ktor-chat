package io.ktor.chat.client

import io.ktor.chat.*
import io.ktor.chat.Nothing
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.sse.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

inline fun <reified E: Identifiable<ID>, ID> restRepository(
    crossinline http: () -> HttpClient,
    path: String,
) = object : Repository<E, ID> {

    override suspend fun get(id: ID): E? =
        http().get("$path/$id").body()

    override suspend fun create(e: E): E =
        http().post(path) {
            setBody(e)
        }.body()

    override suspend fun update(e: E) {
        http().put("$path/${e.id}") {
            setBody(e)
        }
    }

    override suspend fun delete(id: ID) {
        http().delete("$path/$id")
    }

    override suspend fun list(query: Query): List<E> =
        if (query == Nothing)
            emptyList()
        else http().get("$path${query.toQueryString()}").body()

    private fun Query.toQueryString() =
        when(this) {
            is Everything, Nothing -> ""
            is MapQuery -> '?' + parametersOf(
                mapValues { (_, values) -> values.map { it.toString() }  }
            ).formUrlEncode()
        }
}

inline fun <reified E : Identifiable<ID>, ID> observableRepository(
    crossinline http: () -> HttpClient,
    path: String,
): ObservableRepository<E, ID> =
    object : Repository<E, ID> by restRepository(http, path), ObservableRepository<E, ID> {
        val observers = mutableMapOf<Observer<E>, Job>()

        override fun onChange(observer: Observer<E>): Observer<E> {
            observers[observer] = http().run {
                launch {
                    sse("$path/changes") {
                        incoming.collect { event ->
                            event.data?.let { data ->
                                val (changeTypeName, entityJson) = data.split(" ", limit = 2)
                                val changeType = ChangeType.valueOf(changeTypeName)
                                val entity = Json.decodeFromString<E>(entityJson)
                                observer(changeType, entity)
                            }
                        }
                    }
                }
            }
            return observer
        }

        override fun forget(observer: Observer<E>) {
            observers.remove(observer)?.cancel()
        }
    }