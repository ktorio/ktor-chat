package io.ktor.chat.client

import io.ktor.chat.*
import io.ktor.server.config.yaml.*
import io.ktor.server.testing.*
import kotlinx.datetime.Clock
import io.ktor.chat.client.HttpChatClient.Companion.configureForChat
import kotlin.test.Ignore
import kotlin.test.Test

class HttpChatClientTest {

    @Ignore // TODO
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
        
        HttpChatClient(client.configureForChat()).apply {
            val (name, email, password) = testUser
            register(serverUrl, email, name, password)
            login(serverUrl, testUser.email, testUser.password).apply {
                testUser = FullUser(name, email, password, user.id)
            }
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