package io.ktor.chat.server

import io.ktor.di.*
import io.ktor.chat.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.routing.*

fun Application.rooms() {
    val rooms: Repository<Room, Long> by dependencies

    routing {
        authenticate {
            route("/rooms") {
                restGet(rooms)
                restMutations(rooms)
            }
        }
    }
}