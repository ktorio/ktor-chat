package io.ktor.chat.vm

import androidx.compose.runtime.Composable
import io.ktor.chat.client.ChatClient

@Composable
expect fun createViewModel(chatClient: ChatClient? = null): ChatViewModel