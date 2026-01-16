package io.ktor.chat

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.plugins.di.*
import io.ktor.server.routing.*

fun Application.messages(messages: ObservableRepository<Message, Long>) {
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