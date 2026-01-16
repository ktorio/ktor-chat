package io.ktor.chat

import io.ktor.http.*
import io.ktor.serialization.kotlinx.KotlinxWebsocketSerializationConverter
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.routing.openapi.describe
import io.ktor.server.sse.*
import io.ktor.server.websocket.WebSockets
import io.ktor.util.*
import io.ktor.utils.io.ExperimentalKtorApi
import kotlinx.serialization.json.Json

fun Application.rest() {
    install(CORS) {
        allowNonSimpleContentTypes = true
        allowCredentials = true
        allowSameOrigin = true
        anyMethod()
        allowXHttpMethodOverride()

        // Allow headers
        allowHeader(HttpHeaders.Authorization)
        allowHeader(HttpHeaders.Upgrade)
        allowHeader(HttpHeaders.Connection)
        allowHeader("Sec-WebSocket-Key")
        allowHeader("Sec-WebSocket-Version")
        allowHeader("Sec-WebSocket-Extensions")
        allowHeader("Sec-WebSocket-Protocol")
        allowHeader("Sec-WebSocket-Accept") // Important for WebSocket handshake
        allowHeaders { true }
        anyHost()
    }
    install(ContentNegotiation) {
        json(Json {
            encodeDefaults = false
            ignoreUnknownKeys = true
            isLenient = true
            prettyPrint = true
        })
    }
    install(StatusPages) {
        exception<ConflictingArgumentException> { call, ex ->
            call.respondText(ContentType.Text.Plain, HttpStatusCode.Conflict) {
                ex.message ?: "Duplicate value!"
            }
        }
        exception<IllegalArgumentException> { call, ex ->
            call.respondText(ContentType.Text.Plain, HttpStatusCode.BadRequest) {
                ex.message ?: "Bad input!"
            }
        }
        exception<Exception> { call, ex ->
            this@rest.environment.log.error("Internal server error, returning 500", ex)
            call.respondText(ContentType.Text.Plain, HttpStatusCode.InternalServerError) {
                "Internal error!"
            }
        }
    }
    install(SSE)
    install(WebSockets) {
        contentConverter = KotlinxWebsocketSerializationConverter(signalingCommandsFormat)
    }
}

@OptIn(ExperimentalKtorApi::class)
inline fun <reified E : Identifiable<Long>> Route.restGet(
    repository: Repository<E, Long>
) {
    get {
        val query = MapQuery.of(call.queryParameters.toMap())
        call.respond(repository.list(query))
    }.describe {
        summary = "Get list of ${E::class.simpleName?.lowercase()}s"
    }
}

@OptIn(ExperimentalKtorApi::class)
inline fun <reified E : Identifiable<Long>> Route.restMutations(
    repository: Repository<E, Long>
) {
    post {
        val newEntity = repository.create(call.receive())
        call.respond(newEntity)
    }.describe {
        summary = "Create a ${E::class.simpleName?.lowercase()}"
    }
    put("{id}") {
        val entity = call.receive<E>()
        repository.update(entity)

        call.respond(HttpStatusCode.NoContent)
    }.describe {
        summary = "Update a ${E::class.simpleName?.lowercase()}"
    }
    delete("{id}") {
        val id = call.parameters["id"]?.toLongOrNull()
            ?: throw NotFoundException()
        repository.delete(id)

        call.respond(HttpStatusCode.NoContent)
    }.describe {
        summary = "Delete a ${E::class.simpleName?.lowercase()}"
    }
}
