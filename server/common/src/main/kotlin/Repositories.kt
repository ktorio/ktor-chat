package io.ktor.chat

import io.ktor.server.application.*
import io.ktor.server.plugins.di.*

fun Application.repositories() {
    val log = environment.log

    dependencies {
        provide<Repository<FullUser, Long>>(UserRepository::class)
        provide<Repository<Room, Long>>(RoomRepository::class)
        provide<ObservableRepository<Membership, Long>> {
            MemberRepository(resolve()).observable(onFailure = { e ->
                log.error("Failed to subscribe to event", e)
            })
        }
        provide<ObservableRepository<Message, Long>> {
            MessageRepository(resolve()).observable(onFailure = { e ->
                log.error("Failed to subscribe to event", e)
            })
        }
    }
}