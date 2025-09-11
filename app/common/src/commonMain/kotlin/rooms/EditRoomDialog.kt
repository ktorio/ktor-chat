package io.ktor.chat.rooms

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import io.ktor.chat.*
import io.ktor.chat.utils.tryRequest

@Composable
fun EditRoomDialog(
    room: Room,
    onEdit: suspend (Room) -> Unit,
    onClose: () -> Unit
) {
    var name by mutableStateOf(room.name)
    val coroutineScope = rememberCoroutineScope()

    AlertDialog(
        title = { Text("Edit room") },
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
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    coroutineScope.tryRequest {
                        onEdit(room.copy(
                            name = name,
                        ))
                    }
                    onClose()
                },
                enabled = name.isNotBlank(),
            ) {
                Text("Save")
            }
        }
    )
}