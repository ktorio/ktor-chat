package io.ktor.chat.rooms

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import io.ktor.chat.*
import kotlinx.coroutines.launch
import io.ktor.chat.components.ChatIcons
import io.ktor.chat.components.RemoteLoader
import io.ktor.chat.utils.Remote

@Composable
fun JoinRoomDialog(
    joinedRooms: List<Membership>,
    searchRooms: @Composable (String) -> State<Remote<List<Room>>>,
    onJoin: suspend (Room) -> Unit,
    onClose: () -> Unit
) {
    var search by remember { mutableStateOf("") }
    var selected by mutableStateOf<Room?>(null)
    val roomsRemote by searchRooms(search) // TODO debounce
    val coroutineScope = rememberCoroutineScope()

    AlertDialog(
        title = { Text("Join a room") },
        onDismissRequest = { onClose() },
        text = {
            Column {
                TextField(
                    value = search,
                    label = { Text("Search") },
                    modifier = Modifier.fillMaxWidth(),
                    onValueChange = { search = it },
                    singleLine = true,
                )
                RemoteLoader(roomsRemote) { allRooms ->
                    val rooms = allRooms - joinedRooms.map { it.room }
                    NavigationRail {
                        rooms.forEachIndexed { index, room ->
                            NavigationRailItem(
                                icon = { Icon(ChatIcons.Room, contentDescription = room.name) },
                                label = { Text(room.name) },
                                selected = room == selected,
                                onClick = { selected = room },
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    coroutineScope.launch {
                        onJoin(selected!!)
                        onClose()
                    }
                },
                enabled = selected != null,
            ) {
                Text("Join")
            }
        }
    )
}