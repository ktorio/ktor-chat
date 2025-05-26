package io.ktor.chat.client

import io.ktor.chat.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.sse.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*

/**
 * Implementation of ChatClient using Ktor HTTP client for chat server API.
 *
 * TODO handle unauthenticated responses
 */
class HttpChatClient(
    private var http: HttpClient = HttpClient(CIO).configureForChat(server = null, token = null)
): ChatClient {

    constructor(server: String?, token: String?): this(HttpClient(CIO).configureForChat(server, token))

    companion object {
        /**
         * Installs baseline configuration for client, including authentication when relevant.
         */
        fun HttpClient.configureForChat(
            server: String? = null,
            token: String? = null,
        ): HttpClient = config {
            expectSuccess = true

            install(SSE)
            install(ContentNegotiation) {
                json()
            }
            install(DefaultRequest) {
                contentType(ContentType.Application.Json)
                accept(ContentType.Application.Json)
                if (server != null)
                    url(server)
                if (token != null)
                    header(HttpHeaders.Authorization, "Bearer $token")
            }
        }
    }

    override suspend fun verify(): Boolean {
        return http.get("/auth/verify").status.isSuccess()
    }

    override suspend fun login(server: String, email: String, password: String): LoginResponse {
        val authentication = http.post("$server/auth/login") {
            setBody(LoginRequest(email, password))
        }.body<LoginResponse>()

        http = HttpClient(CIO).configureForChat(server, authentication.token)
        return authentication
    }
    
    override suspend fun register(
        server: String,
        email: String,
        name: String,
        password: String
    ): RegistrationResponse {
        val registration = http.post("$server/auth/register") {
            setBody(RegistrationRequest(name, email, password))
        }.body<RegistrationResponse>()

        http = HttpClient(CIO).configureForChat(server, registration.token)
        return registration
    }

    override suspend fun confirm(code: String) {
        http.post("/auth/confirm") {
            setBody(ConfirmationRequest(code))
        }
    }

    override suspend fun logout(server: String) {
        http.post("$server/auth/logout")
        http = HttpClient(CIO).configureForChat(server, token = null)
    }

    override suspend fun isServerAvailable(server: String): Boolean =
        try {
            http.get("$server/ping").let { response ->
                if (response.status.isSuccess())
                    response.bodyAsText() == "pong"
                else false
            }
        } catch (_: Exception) {
            false
        }

    override val rooms: Repository<Room, Long> get() = restRepository({ http }, "rooms")
    override val messages: ObservableRepository<Message, Long> get() = observableRepository({ http }, "messages")
    override val users: ReadOnlyRepository<SimplifiedUser, Long> get() = restRepository({ http }, "users")
    override val memberships: ObservableRepository<Membership, Long> get() = observableRepository({ http }, "memberships")
}

