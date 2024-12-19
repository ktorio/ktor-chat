package io.ktor.chat.server

import io.ktor.chat.*
import io.ktor.di.dependencies
import io.ktor.di.invoke
import io.ktor.di.provide
import io.ktor.di.resolve
import io.ktor.server.application.*

fun Application.repositories() {
    dependencies {
        provide<Repository<FullUser, Long>> {
            MailingUserRepository(
                delegate = UserRepository(resolve()),
                mailer = environment.log.logMailer(),
                algorithm = resolve("hash"),
            )
        }
        provide<Repository<Room, Long>>(RoomRepository::class)
        provide<ObservableRepository<Membership, Long>> {
            MemberRepository(resolve()).observable(onFailure = { e ->
                environment.log.error("Failed to subscribe to event", e)
            })
        }
        provide<ObservableRepository<Message, Long>> {
            MessageRepository(resolve()).observable(onFailure = { e ->
                environment.log.error("Failed to subscribe to event", e)
            })
        }
    }
}