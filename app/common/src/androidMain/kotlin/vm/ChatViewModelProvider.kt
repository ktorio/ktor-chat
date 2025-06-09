package io.ktor.chat.vm

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import io.ktor.chat.client.ChatClient

@Composable
actual fun createViewModel(chatClient: ChatClient?): ChatViewModel {
    return viewModel {
        val server = "http://10.0.2.2:8080"
        if (chatClient != null) {
            ChatViewModel(server = server, client = chatClient)
        } else {
            ChatViewModel(server = server)
        }
    } // TODO save state
}