package io.ktor.chat.rooms

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import io.ktor.chat.*
import kotlinx.coroutines.launch
import io.ktor.chat.components.ChatIcons
import io.ktor.chat.utils.tryRequest

@Composable
fun RoomHeader(
    modifier: Modifier = Modifier,
    membership: Membership,
    onLeaveRoom: (suspend (Membership) -> Unit)?,
    onUpdateRoom: (suspend (Room) -> Unit)?,
    onDeleteRoom: (suspend (Room) -> Unit)?,
    onVideoCallInitiated: (suspend () -> Unit)?,
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

            Spacer(Modifier.weight(1f))

            // Video call button
            if (onVideoCallInitiated != null) {
                IconButton(onClick = {
                    coroutineScope.launch { onVideoCallInitiated() }
                }) {
                    Icon(
                        imageVector = Icons.Default.Videocam,
                        contentDescription = "Video Call",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
        DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
            if (onUpdateRoom != null) {
                DropdownMenuItem(
                    text = { Text("Edit") },
                    leadingIcon = { Icon(Icons.Default.Edit, contentDescription = "Edit") },
                    onClick = { editDialogExpanded = true; menuExpanded = false })
            }
            if (onDeleteRoom != null) {
                DropdownMenuItem(
                    text = { Text("Delete", color = MaterialTheme.colorScheme.error) },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.error
                        )
                    },
                    onClick = {
                        coroutineScope.tryRequest {
                            onDeleteRoom(membership.room)
                        }
                    })
            }

            HorizontalDivider()

            if (onLeaveRoom != null) {
                DropdownMenuItem(
                    text = { Text("Leave") },
                    leadingIcon = { Icon(Icons.Default.Remove, contentDescription = "Leave") },
                    onClick = {
                        coroutineScope.tryRequest {
                            onLeaveRoom(membership)
                        }
                    })
            }
        }

        if (editDialogExpanded) {
            EditRoomDialog(
                room = membership.room,
                onEdit = { onUpdateRoom!!(it) },
                onClose = { editDialogExpanded = false }
            )
        }
    }
}
