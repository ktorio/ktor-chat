package ktor.chat.vm

import androidx.compose.runtime.Composable
import ktor.chat.client.ChatClient

@Composable
expect fun createViewModel(chatClient: ChatClient? = null): ChatViewModel