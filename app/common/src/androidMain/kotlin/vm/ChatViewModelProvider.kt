package io.ktor.chat.vm

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import io.ktor.chat.app.BuildKonfig
import io.ktor.chat.client.*

@Composable
actual fun createViewModel(chatClient: ChatClient?): ChatViewModel {
    return viewModel {
        val server = BuildKonfig.SERVER_URL
        if (chatClient != null) {
            ChatViewModel(server = server, client = chatClient)
        } else {
            ChatViewModel(server = server)
        }
    } // TODO save state
}