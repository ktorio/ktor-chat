package io.ktor.chat.server

import io.ktor.chat.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.plugins.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.core.qualifier.named
import org.koin.ktor.ext.inject

fun Application.members() {
    val memberships by inject<ObservableRepository<Membership, Long>>(named("memberships"))

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