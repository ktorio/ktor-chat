package io.ktor.chat.server

import io.ktor.chat.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.plugins.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.kodein.di.instance
import org.kodein.di.ktor.closestDI

fun Application.members() {
    val memberships by closestDI().instance<ObservableRepository<Membership, Long>>()

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