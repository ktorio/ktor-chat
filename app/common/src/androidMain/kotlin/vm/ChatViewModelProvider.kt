package io.ktor.chat.vm

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import io.ktor.chat.client.ChatClient
import io.ktor.chat.client.serverHost

@Composable
actual fun createViewModel(chatClient: ChatClient?): ChatViewModel {
    return viewModel {
        val server = "http://${serverHost}:8080"
        if (chatClient != null) {
            ChatViewModel(server = server, client = chatClient)
        } else {
            ChatViewModel(server = server)
        }
    } // TODO save state
}