package io.ktor.chat

import io.ktor.openapi.*
import io.ktor.server.application.*
import io.ktor.server.plugins.openapi.*
import io.ktor.server.plugins.swagger.*
import io.ktor.server.routing.*

fun Application.docs() {
    routing {
        openAPI("/openapi") {
            info = OpenApiInfo("Ktor Chat", "1.0")
        }

        swaggerUI("/swagger") {
            info = OpenApiInfo("Ktor Chat", "1.0")
        }
    }
}
