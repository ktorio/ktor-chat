package io.ktor.chat.server

import io.ktor.chat.*
import io.ktor.server.application.*
import org.jetbrains.exposed.sql.Database
import org.kodein.di.*
import org.kodein.di.ktor.*
import org.kodein.di.ktor.closestDI

fun Application.repositories() {
    val database by closestDI().instance<Database>()

    di {
        // Note: tag is required because generics don't work well with DI
        bind<Repository<FullUser, Long>>() with singleton { 
            UserRepository(database) 
        }
        bind<ObservableRepository<Message, Long>>() with singleton { 
            MessageRepository(database).observable(onFailure = { e ->
                environment.log.error("Failed to subscribe to event", e)
            })
        }
        bind<Repository<Room, Long>>() with singleton { 
            RoomRepository(database) 
        }
        bind<ObservableRepository<Membership, Long>>() with singleton { 
            MemberRepository(database).observable(onFailure = { e ->
                environment.log.error("Failed to subscribe to event", e)
            })
        }
    }
}
