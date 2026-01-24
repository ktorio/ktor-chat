package io.ktor.chat.client

import io.ktor.chat.*
import kotlin.time.Clock
import kotlin.time.Duration.Companion.seconds

class MockChatClient(
    rootUser: FullUser = FullUser("Steve", "steve@mail.com", "kek"),
    lobby: Room = Room("lobby", id = 1u)
) : ChatClient {
    override suspend fun verify(): Boolean = true

    override suspend fun login(server: String, email: String, password: String): LoginResponse =
        users.list().find {
            it.email == email && it.password == password
        }?.let {
            LoginResponse("abc123def456", it)
        } ?: error("User not found")

    override suspend fun register(
        server: String,
        email: String,
        name: String,
        password: String
    ): RegistrationResponse {
        val user = FullUser(name, email, password)
        users.create(user)
        return RegistrationResponse("abc123def456", user, "123456")
    }

    override suspend fun confirm(code: String) {
        // success always
    }

    override suspend fun isServerAvailable(server: String): Boolean = true

    override suspend fun logout(server: String) {
        // no sessions here yet
    }

    override val users = ListRepository(
        rootUser,
        copy = { u, id -> u.copy(id = id) }
    )

    override val rooms = ListRepository(lobby, copy = { r, id -> r.copy(id = id) })
    override val messages = ListRepository(
        Message(
            author = rootUser,
            room = lobby.id,
            created = Clock.System.now()
                .minus(42.seconds),
            text = "Hello, World!"
        ),
        copy = { m, id -> m.copy(id = id) }
    ).observable()
    override val memberships: ObservableRepository<Membership, ULong> =
        ListRepository<Membership>(copy = { m, id -> m.copy(id = id) }).observable()
}