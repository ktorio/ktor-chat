package io.ktor.chat

import io.ktor.chat.*
import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.sse.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.config.*
import io.ktor.server.testing.*
import io.ktor.util.*

val mockUser = FullUser("Joe", "joey.bloggs@mail.com", "pwd")
val mockAuthorization = with(mockUser) { "Basic ${"$email:$password".encodeBase64()}" }

fun Application.mockAuth() {
    authentication {
        basic {
           validate {
               ChatPrincipal(mockUser)
           }
        }
    }
}

fun HttpClient.configureForEvents() =
    config {
        install(SSE) {
            showCommentEvents()
            showRetryEvents()
        }
        install(DefaultRequest) {
            header(HttpHeaders.Authorization, mockAuthorization)
        }
    }

fun HttpClient.configureForTest() =
    config {
        install(ContentNegotiation) {
            json()
        }
        install(SSE) {
            showCommentEvents()
            showRetryEvents()
        }
        install(DefaultRequest) {
            contentType(ContentType.Application.Json)
            header(HttpHeaders.Authorization, mockAuthorization)
        }
    }

fun ApplicationTestBuilder.configureYaml(yamlFile: String?) {
    if (yamlFile == null)
        return
    environment {
        config = ApplicationConfig(yamlFile)
    }
}

