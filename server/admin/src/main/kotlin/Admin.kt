package io.ktor.chat

import io.ktor.server.plugins.di.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.routing.*

fun Application.admin() {
    val users: Repository<FullUser, ULong> by dependencies

    routing {
        authenticate {
            route("/users") {
                restMutations(users)
            }
        }
    }
}