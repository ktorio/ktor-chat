package io.ktor.chat

import io.ktor.openapi.*
import io.ktor.server.application.*
import io.ktor.server.plugins.swagger.*
import io.ktor.server.routing.*

fun Application.docs() {
    routing {
        swaggerUI("/swagger") {
            info = OpenApiInfo("My API", "1.0")
        }
    }
}