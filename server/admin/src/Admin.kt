package io.ktor.chat.server

import io.ktor.chat.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Application.adminModule() {
    val users by inject<Repository<FullUser, Long>>()

    routing {
        authenticate {
            route("/users") {
                restMutations(users)
            }
        }
    }
}