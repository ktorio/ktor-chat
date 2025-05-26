package io.ktor.chat.vm

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import io.ktor.chat.*
import io.ktor.chat.client.ChatClient
import io.ktor.chat.client.ChatRestClient
import io.ktor.chat.client.HttpChatClient
import io.ktor.chat.client.ServerStatusClient

class ChatViewModel(
    server: String,
    token: String?,
    loggedInUser: User?,
    room: Membership?,
    private val client: ChatClient = HttpChatClient(
        server = server,
        token = token,
    ),
): ViewModel(), ServerStatusClient by client, ChatRestClient by client {
    val server = mutableStateOf(server)
    val token = mutableStateOf(token)
    val loggedInUser = mutableStateOf(loggedInUser)
    val confirmation = mutableStateOf(
        if (loggedInUser == null)
            Confirmation.Unauthenticated
        else Confirmation.Confirmed
    )
    val room = mutableStateOf(room)
    val screenSize = mutableStateOf(Pair(-1, -1))

    suspend fun verify(): Boolean =
        try {
            client.verify()
        } catch (e: Throwable) {
            e.printStackTrace()
            false
        }

    suspend fun login(server: String, email: String, password: String) {
        val authentication = client.login(server, email, password)

        this.server.value = server
        this.token.value = authentication.token
        this.loggedInUser.value = authentication.user
    }
    
    suspend fun register(server: String, email: String, name: String, password: String) {
        val response = client.register(server, email, name, password)

        this.server.value = server
        this.token.value = response.token
        this.loggedInUser.value = response.user
        this.confirmation.value = Confirmation.Pending(response.code ?: "")
    }

    suspend fun confirm(code: String) {
        client.confirm(code)
        confirmation.value = Confirmation.Confirmed
    }

    suspend fun logout() {
        loggedInUser.value = null
        token.value = null
        room.value = null
        confirmation.value = Confirmation.Unauthenticated
        client.logout(server.value)
    }
}

sealed interface Confirmation {
    data object Unauthenticated: Confirmation
    data class Pending(val code: String): Confirmation
    data object Confirmed: Confirmation
}