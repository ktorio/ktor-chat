package io.ktor.chat.server

import io.ktor.chat.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.routing.*
import org.kodein.di.instance
import org.kodein.di.ktor.closestDI

fun Application.messages() {
    val messages by closestDI().instance<ObservableRepository<Message, Long>>()

    routing {
        authenticate {
            route("/messages") {
                restGet(messages)
                restMutations(messages)
                sseChanges(messages)
            }
        }
    }
}