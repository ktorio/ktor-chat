package io.ktor.chat.server

import io.ktor.chat.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.di.*
import io.ktor.server.testing.*
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals

class AuthenticationTest {

    @Test
    fun `registration and login works`() = authenticationTest {
        client.config {
            install(ContentNegotiation) {
                json()
            }
            install(DefaultRequest) {
                contentType(ContentType.Application.Json)
            }
        }.apply {
            // User is registered
            val registrationResponse = post("/auth/register") {
                setBody(RegistrationRequest(
                    "Joey Bloggs",
                    "joey@example.com",
                    "password123",
                ))
            }.apply {
                assertEquals(HttpStatusCode.OK, status)
            }.body<RegistrationResponse>()

            // Confirmation code works
            post("/auth/confirm") {
                header(HttpHeaders.Authorization, "Bearer ${registrationResponse.token}")
                setBody(ConfirmationRequest(registrationResponse.code))
            }.apply {
                assertEquals(HttpStatusCode.NoContent, status)
            }

            // User can log in
            val authResponse = post("/auth/login") {
                setBody(LoginRequest(
                    "joey@example.com",
                    "password123",
                ))
            }.apply {
                assertEquals(HttpStatusCode.OK, status)
            }.body<LoginResponse>()

            // And find themselves from the list endpoint
            get("/users") {
                header(HttpHeaders.Authorization, "Bearer ${authResponse.token}")
            }.apply {
                assertEquals(HttpStatusCode.OK, status)
                assertContains(bodyAsText(), "Joey Bloggs", message = "Should contain newly registered user")
            }
        }
    }

    @Test
    fun `login fails with missing user`() = authenticationTest {
        client.post("/auth/login") {
            contentType(ContentType.Application.Json)
            setBody("""
                {
                  "email": "not.a.user@example.com",
                  "password": "password123"
                }
            """.trimIndent())
        }.apply {
            assertEquals(HttpStatusCode.Unauthorized, status)
        }
    }

    private fun authenticationTest(block: suspend ApplicationTestBuilder.() -> Unit) =
        testApplication {
            configureYaml(yamlFile = "auth-config.yaml")
            application {
                root()
                security()
                mail()
                rest()
                mockUsersRepository()
                auth()
                users()
            }
            this.block()
        }

}

private fun Application.mockUsersRepository() {
    dependencies {
        provide<Repository<FullUser, Long>> {
            ListRepository.create()
        }
    }
}