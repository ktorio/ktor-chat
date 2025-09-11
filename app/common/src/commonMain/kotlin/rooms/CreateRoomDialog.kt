package io.ktor.chat.rooms

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import io.ktor.chat.components.ErrorText
import io.ktor.chat.utils.tryRequest

@Composable
fun CreateRoomDialog(
    onCreate: suspend (String) -> Unit,
    onClose: () -> Unit
) {
    var name by mutableStateOf("")
    val loading = mutableStateOf(false)
    var error by mutableStateOf<String?>(null)
    val coroutineScope = rememberCoroutineScope()

    fun createRoom() {
        coroutineScope.tryRequest(loading, { error = it }) {
            onCreate(name)
            onClose()
        }
    }
    
    AlertDialog(
        title = { Text("Create room") },
        onDismissRequest = { onClose() },
        text = {
            Column {
                TextField(
                    value = name,
                    label = { Text("Name") },
                    modifier = Modifier.fillMaxWidth(),
                    onValueChange = { name = it },
                    singleLine = true,
                )
                error?.let {
                    Spacer(Modifier.height(12.dp))
                    ErrorText(it)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onClose) {
                Text("Cancel")
            }
        },
        confirmButton = {
            Button(::createRoom, enabled = name.isNotBlank() && !loading.value) {
                Text(if (loading.value) "Sending..." else "Save")
            }
        }
    )
}