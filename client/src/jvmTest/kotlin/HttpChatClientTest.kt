package io.ktor.chat.client

import io.ktor.chat.*
import io.ktor.server.config.yaml.*
import io.ktor.server.testing.*
import kotlinx.datetime.Clock
import io.ktor.chat.client.HttpChatClient.Companion.configureForChat
import kotlin.test.Test

class HttpChatClientTest {

    @Test
    fun endToEndTest() = testApplication {
        environment {
            config = YamlConfig("test-server.yaml")!!
        }
        
        val serverUrl = "http://localhost"
        var testUser = FullUser(
            "bob",
            "bob@law.blog",
            "pa55word"
        )
        val (name, email, password) = testUser
        
        HttpChatClient(client.configureForChat()).register(serverUrl, email, name, password)
        // Create a new client each time to circumvent the client wrapper from replacing our test client
        val authentication = HttpChatClient(client.configureForChat())
            .login(serverUrl, testUser.email, testUser.password)
        testUser = FullUser(name, email, password, authentication.user.id)
        HttpChatClient(client.configureForChat(token = authentication.token)).apply {
            val room = rooms.create(Room("lobby"))
            val message = messages.create(Message(
                author = testUser,
                room = room.id,
                created = Clock.System.now(),
                text = "Hello, world!"
            ))
            messages.delete(message.id)
            rooms.delete(room.id)
        }
    }
    
}