package ktor.chat.vm

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
actual fun createViewModel(): ChatViewModel {
    return viewModel {
        ChatViewModel(
            server = "http://172.22.144.116:8080",
            token = null,
            loggedInUser = null,
            room = null,
        )
    } // TODO save state
}