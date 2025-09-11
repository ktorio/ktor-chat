package io.ktor.chat.messages

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.NativeKeyEvent
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import io.ktor.chat.utils.insert
import io.ktor.chat.utils.tryRequest

@Composable
fun MessageInput(modifier: Modifier = Modifier, send: suspend (String) -> Unit) {
    val coroutineScope = rememberCoroutineScope()
    var message by remember { mutableStateOf(TextFieldValue("")) }
    val loading = remember { mutableStateOf(false) }

    // TODO handle errors
    fun sendMessage(): Boolean {
        if (message.text.isBlank()) return true
        val messageToSend = message.text.trim().also {
            message = TextFieldValue("")
        }
        coroutineScope.tryRequest(loading) {
            send(messageToSend.trim())
        }
        return true
    }
    
    Spacer(Modifier.height(10.dp))

    TextField(
        value = message,
        placeholder = { Text("Type a message...") },
        onValueChange = { message = it },
        modifier = modifier.fillMaxWidth().onPreviewKeyEvent { e ->
            if (e.key != Key.Enter) false
            else {
                if (!e.isShiftPressed) {
                    sendMessage()
                } else if (e.type == KeyEventType.KeyDown) {
                    message = message.insert("\n")
                }
                true
            }
        },
        trailingIcon = {
            IconButton(onClick = { sendMessage() }) {
                Icon(
                    Icons.AutoMirrored.Rounded.Send,
                    contentDescription = "Send message"
                )
            }
        }
    )
}