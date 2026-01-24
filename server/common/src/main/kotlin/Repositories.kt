package io.ktor.chat

import io.ktor.server.application.*
import io.ktor.server.plugins.di.*

fun Application.repositories() {
    val log = environment.log

    dependencies {
        provide<Repository<FullUser, ULong>>(UserRepository::class)
        provide<Repository<Room, ULong>>(RoomRepository::class)
        provide<ObservableRepository<Membership, ULong>> {
            MemberRepository(resolve()).observable(onFailure = { e ->
                log.error("Failed to subscribe to event", e)
            })
        }
        provide<ObservableRepository<Message, ULong>> {
            MessageRepository(resolve()).observable(onFailure = { e ->
                log.error("Failed to subscribe to event", e)
            })
        }
    }
}