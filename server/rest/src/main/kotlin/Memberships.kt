package io.ktor.chat

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.plugins.*
import io.ktor.server.plugins.di.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.members() {
    val memberships: ObservableRepository<Membership, Long> by dependencies

    routing {
        authenticate {
            route("/memberships") {
                get {
                    val userId = call.principal<ChatPrincipal>()?.user?.id
                        ?: throw BadRequestException("You must log in to get a list of joined rooms")
                    call.respond(memberships.list { it["user"] = userId })
                }
                restMutations(memberships)
                sseChanges(memberships)
            }
        }
    }
}