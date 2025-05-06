package ktor.chat

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.runtime.Composable
import ktor.chat.vm.createViewModel

@Preview
@Composable
fun ChatScreenPreview() {
    ChatScreen(createViewModel())
}