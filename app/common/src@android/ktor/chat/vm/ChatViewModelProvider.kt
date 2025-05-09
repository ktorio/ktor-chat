package ktor.chat.vm

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
actual fun createViewModel(): ChatViewModel {
    return viewModel {
        ChatViewModel(
            server = "http://10.0.2.2:8080",
            token = null,
            loggedInUser = null,
            room = null,
        )
    } // TODO save state
}