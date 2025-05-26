package io.ktor.chat.client

import io.ktor.chat.*

interface ChatClient :
        AuthClient,
        ServerStatusClient,
        ChatRestClient

interface AuthClient {
    suspend fun verify(): Boolean
    suspend fun login(server: String, email: String, password: String): LoginResponse
    suspend fun register(server: String, email: String, name: String, password: String): RegistrationResponse
    suspend fun confirm(code: String)
    suspend fun logout(server: String)
}

interface ServerStatusClient {
    suspend fun isServerAvailable(server: String): Boolean
}

interface ChatRestClient {
    val rooms: Repository<Room, Long>
    val messages: ObservableRepository<Message, Long>
    val users: ReadOnlyRepository<User, Long>
    val memberships: ObservableRepository<Membership, Long>
}