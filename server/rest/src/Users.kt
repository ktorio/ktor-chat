package io.ktor.chat.server

import io.ktor.chat.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.core.qualifier.named
import org.koin.ktor.ext.inject

fun Application.users() {
    val users by inject<Repository<FullUser, Long>>(named("users"))

    routing {
        authenticate {
            route("/users") {
                get {
                    call.respond(users.list().map {
                        SimplifiedUser(it.id, it.name)
                    })
                }
            }
        }
    }
}