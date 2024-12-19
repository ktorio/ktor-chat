package io.ktor.chat

import io.ktor.di.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.routing.*

fun Application.adminModule() {
    val users: Repository<FullUser, Long> by dependencies

    routing {
        authenticate {
            route("/users") {
                restMutations(users)
            }
        }
    }
}