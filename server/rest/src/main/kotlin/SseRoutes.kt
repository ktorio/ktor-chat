package io.ktor.chat

import io.ktor.server.request.*
import io.ktor.server.routing.*
import io.ktor.server.sse.*
import io.ktor.sse.*
import kotlinx.coroutines.awaitCancellation
import kotlinx.serialization.json.Json

inline fun <reified E: Identifiable<ULong>> Route.sseChanges(observableRepository: ObservableRepository<E, ULong>) {
    sse("changes") {
        environment.log.info("SSE subscribe ${call.request.path()}")
        val observer = observableRepository.onChange { type, e ->
            val data = "${type.name} ${Json.encodeToString(e)}"
            environment.log.debug("SSE event $data")
            send(ServerSentEvent(data))
        }
        try {
            awaitCancellation()
        } finally {
            environment.log.info("SSE cancel ${call.request.path()}")
            observableRepository.forget(observer)
        }
    }
}