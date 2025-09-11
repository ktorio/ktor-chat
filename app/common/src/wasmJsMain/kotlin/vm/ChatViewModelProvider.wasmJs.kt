package io.ktor.chat.vm

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import io.ktor.chat.app.BuildKonfig
import io.ktor.chat.client.ChatClient

@Composable
actual fun createViewModel(chatClient: ChatClient?): ChatViewModel {
    return viewModel {
        if (chatClient != null) {
            ChatViewModel(server = BuildKonfig.SERVER_URL, client = chatClient)
        } else {
            ChatViewModel(server = BuildKonfig.SERVER_URL)
        }
    } // TODO save state
}