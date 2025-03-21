package io.ktor.chat.server

import io.ktor.chat.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.routing.*
import org.kodein.di.instance
import org.kodein.di.ktor.closestDI

fun Application.adminModule() {
    val users by closestDI().instance<Repository<FullUser, Long>>()

    routing {
        authenticate {
            route("/users") {
                restMutations(users)
            }
        }
    }
}