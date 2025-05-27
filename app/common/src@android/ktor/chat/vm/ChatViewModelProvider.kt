package ktor.chat.vm

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import ktor.chat.client.ChatClient

@Composable
actual fun createViewModel(chatClient: ChatClient?): ChatViewModel {
    return viewModel {
        // proxy to localhost that used in the Android emulator
        val server = "http://10.0.2.2:8080"
        if (chatClient != null) {
            ChatViewModel(server = server, client = chatClient)
        } else {
            ChatViewModel(server = server)
        }
    } // TODO save state
}