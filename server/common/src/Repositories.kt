package io.ktor.chat.server

import io.ktor.chat.*
import io.ktor.server.application.*
import org.jetbrains.exposed.sql.Database
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.koin.ktor.ext.inject
import org.koin.ktor.plugin.koin

fun Application.repositories() {
    val database by inject<Database>()

    koin {
        modules(module {
            // Note: qualifier is required because generics don't work with Koin
            single<Repository<FullUser, Long>>(named("users")) {
                UserRepository(database)
            }
            single<ObservableRepository<Message, Long>>(named("messages")) {
                MessageRepository(database).observable(onFailure = { e ->
                    environment.log.error("Failed to subscribe to event", e)
                })
            }
            single<Repository<Room, Long>>(named("rooms")) {
                RoomRepository(database)
            }
            single<ObservableRepository<Membership, Long>>(named("memberships")) {
                MemberRepository(database).observable(onFailure = { e ->
                    environment.log.error("Failed to subscribe to event", e)
                })
            }
        })
    }
}