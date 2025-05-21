package ktor.chat.client

import io.ktor.chat.*
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.minus

class MockChatClient(
    rootUser: FullUser = FullUser("Steve", "steve@mail.com", "kek"),
    lobby: Room = Room("lobby", id = 1)
): ChatClient {
    override suspend fun verify(): Boolean = true

    override suspend fun login(server: String, email: String, password: String): LoginResponse =
        users.list().find {
            it.email == email && it.password == password
        }?.let {
            LoginResponse("abc123def456", it)
        } ?: throw IllegalAccessException()

    override suspend fun register(server: String, email: String, name: String, password: String): RegistrationResponse {
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
        rootUser
    )

    override val rooms = ListRepository(lobby)
    override val messages = ListRepository(
        Message(
            author = rootUser,
            room = lobby.id,
            created = Clock.System.now()
                .minus(42, DateTimeUnit.SECOND),
            text = "Hello, World!"
        )
    ).observable()
    override val memberships: ObservableRepository<Membership, Long> = ListRepository<Membership>().observable()
}