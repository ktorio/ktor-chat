package io.ktor.chat

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment.Companion.BottomCenter
import androidx.compose.ui.Alignment.Companion.Center
import androidx.compose.ui.Alignment.Companion.TopCenter
import androidx.compose.ui.Alignment.Companion.TopStart
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.datetime.Clock
import io.ktor.chat.client.listInRoom
import io.ktor.chat.client.remoteList
import io.ktor.chat.client.remoteListWithUpdates
import io.ktor.chat.components.RemoteLoader
import io.ktor.chat.messages.MessageInput
import io.ktor.chat.messages.MessageList
import io.ktor.chat.rooms.RoomHeader
import io.ktor.chat.rooms.RoomsMenu
import io.ktor.chat.settings.UserMenu
import io.ktor.chat.utils.Remote
import io.ktor.chat.vm.ChatViewModel
import io.ktor.chat.vm.createViewModel

@Composable
fun ChatScreen(vm: ChatViewModel = createViewModel()) {
    var selectedRoom by remember { vm.room }
    val roomsRemote: Remote<SnapshotStateList<Membership>> by vm.memberships.remoteListWithUpdates()
    val messagesRemote: Remote<SnapshotStateList<Message>> by vm.messages.listInRoom(selectedRoom?.room)
    val smallScreen by remember { derivedStateOf { vm.screenSize.value.first < 1400 } }

    RemoteLoader(roomsRemote) { rooms ->
        RoomsMenu(
            asDropdown = smallScreen,
            joinedRooms = rooms,
            searchRooms = { vm.rooms.remoteList() },
            selectedRoom = selectedRoom,
            onSelect = {
                selectedRoom = it
            },
            onJoin = { joinedRoom ->
                selectedRoom = vm.memberships.create(Membership(
                    user = vm.loggedInUser.value!!,
                    room = joinedRoom,
                ))
            },
            onCreate = { newRoomName ->
                vm.rooms.create(Room(newRoomName)).let { newRoom ->
                    selectedRoom = vm.memberships.create(Membership(
                        user = vm.loggedInUser.value!!,
                        room = newRoom,
                    ))
                }
            },
            sideMenu = {
                UserMenu(vm)
            }
        ) {
            MessagesView(
                selectedRoom,
                messagesRemote,
                onLeaveRoom = {
                    vm.memberships.delete(it.id)
                    selectedRoom = null
                },
                onUpdateRoom = {
                    vm.rooms.update(it)
                    selectedRoom = selectedRoom?.copy(room = it)
                },
                onDeleteRoom = {
                    vm.rooms.delete(it.id)
                    selectedRoom = null
                }
            ) { messageText ->
                vm.messages.create(
                    Message(
                        author = vm.loggedInUser.value!!,
                        created = Clock.System.now(),
                        room = selectedRoom!!.room.id,
                        text = messageText,
                    )
                )
            }
        }
    }
}

@Composable
private fun MessagesView(
    selectedRoom: Membership?,
    messagesRemote: Remote<SnapshotStateList<Message>>,
    onLeaveRoom: suspend (Membership) -> Unit,
    onUpdateRoom: suspend (Room) -> Unit,
    onDeleteRoom: suspend (Room) -> Unit,
    onCreate: suspend (String) -> Unit,
) {

    when (selectedRoom) {
        null -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Center) {
            Text("Select a room to begin")
        }
        else -> RemoteLoader(messagesRemote) { messages ->
            Box(modifier = Modifier.fillMaxSize()) {
                RoomHeader(modifier = Modifier.align(TopStart).height(50.dp), selectedRoom, onLeaveRoom, onUpdateRoom, onDeleteRoom)
                MessageList(modifier = Modifier.align(TopCenter).padding(top = 50.dp, bottom = 60.dp), messages)
                MessageInput(modifier = Modifier.align(BottomCenter).height(60.dp), onCreate)
            }
        }
    }
}