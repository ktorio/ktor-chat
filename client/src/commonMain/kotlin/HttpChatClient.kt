package io.ktor.chat.client

import io.ktor.chat.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.sse.*
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.KotlinxWebsocketSerializationConverter
import io.ktor.serialization.kotlinx.json.*

expect fun httpEngine(): HttpClientEngineFactory<*>

/**
 * Implementation of ChatClient using Ktor HTTP client for chat server API.
 *
 * TODO handle unauthenticated responses
 */
class HttpChatClient(
    private var http: HttpClient = HttpClient(httpEngine()).configureForChat(server = null, token = null)
) : ChatClient {

    constructor(server: String?, token: String?) : this(HttpClient(httpEngine()).configureForChat(server, token))

    companion object {
        /**
         * Installs baseline configuration for the client, including authentication when relevant.
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
                if (token != null) {
                    header(HttpHeaders.Authorization, "Bearer $token")
                }
            }
            install(WebSockets) {
                contentConverter = KotlinxWebsocketSerializationConverter(signalingCommandsFormat)
            }
        }
    }

    fun getHttp() = http

    override suspend fun verify(): Boolean {
        return http.get("/auth/verify").status.isSuccess()
    }

    override suspend fun login(server: String, email: String, password: String): LoginResponse {
        val authentication = http.post("$server/auth/login") {
            setBody(LoginRequest(email, password))
        }.body<LoginResponse>()

        http = HttpClient(httpEngine()).configureForChat(server, authentication.token)
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

        http = HttpClient(httpEngine()).configureForChat(server, registration.token)
        return registration
    }

    override suspend fun confirm(code: String) {
        http.post("/auth/confirm") {
            setBody(ConfirmationRequest(code))
        }
    }

    override suspend fun logout(server: String) {
        http.post("$server/auth/logout")
        http = HttpClient(httpEngine()).configureForChat(server, token = null)
    }

    override suspend fun isServerAvailable(server: String): Boolean =
        try {
            http.get("$server/ping").let { response ->
                if (response.status.isSuccess())
                    response.bodyAsText() == "pong"
                else false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }

    override val rooms: Repository<Room, ULong> get() = restRepository({ http }, "rooms")
    override val messages: ObservableRepository<Message, ULong> get() = observableRepository({ http }, "messages")
    override val users: ReadOnlyRepository<SimplifiedUser, ULong> get() = restRepository({ http }, "users")
    override val memberships: ObservableRepository<Membership, ULong>
        get() = observableRepository(
            { http },
            "memberships"
        )
}

