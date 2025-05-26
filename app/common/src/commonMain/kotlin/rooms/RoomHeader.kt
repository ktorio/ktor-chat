package io.ktor.chat.rooms

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import io.ktor.chat.*
import kotlinx.coroutines.launch
import io.ktor.chat.components.ChatIcons
import io.ktor.chat.utils.tryRequest
import io.ktor.chat.vm.ChatViewModel

@Composable
fun RoomHeader(
    modifier: Modifier = Modifier,
    membership: Membership,
    onLeaveRoom: suspend (Membership) -> Unit,
    onUpdateRoom: suspend (Room) -> Unit,
    onDeleteRoom: suspend (Room) -> Unit,
) {
    var menuExpanded by remember { mutableStateOf(false) }
    var editDialogExpanded by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    Surface(modifier = modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            TextButton({ menuExpanded = !menuExpanded }) {
                Icon(
                    imageVector = ChatIcons.Room,
                    contentDescription = "Room",
                    modifier = Modifier.size(ButtonDefaults.IconSize)
                )
                Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                Text(membership.room.name)
            }
        }
        DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
            DropdownMenuItem(
                text = { Text("Edit") },
                leadingIcon = { Icon(Icons.Default.Edit, contentDescription = "Edit") },
                onClick = { editDialogExpanded = true; menuExpanded = false })
            DropdownMenuItem(
                text = { Text("Delete", color = MaterialTheme.colorScheme.error) },
                leadingIcon = { Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error) },
                onClick = {
                    coroutineScope.tryRequest {
                        onDeleteRoom(membership.room)
                    }

                })

            HorizontalDivider()

            DropdownMenuItem(
                text = { Text("Leave") },
                leadingIcon = { Icon(Icons.Default.Remove, contentDescription = "Leave") },
                onClick = {
                    coroutineScope.tryRequest {
                        onLeaveRoom(membership)
                    }
                })
        }
        if (editDialogExpanded) {
            EditRoomDialog(
                room = membership.room,
                onEdit = { onUpdateRoom(it) },
                onClose = { editDialogExpanded = false })
        }
    }
}