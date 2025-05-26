package io.ktor.chat

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import io.ktor.chat.vm.createViewModel

fun main() = application {
    Window(title = "KTOR CHAT", onCloseRequest = ::exitApplication) {
        ChatApplication(createViewModel())
    }
}