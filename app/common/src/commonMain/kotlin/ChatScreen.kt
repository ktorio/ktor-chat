package io.ktor.chat

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.BottomCenter
import androidx.compose.ui.Alignment.Companion.TopCenter
import androidx.compose.ui.Alignment.Companion.TopStart
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import io.ktor.chat.client.listInRoom
import io.ktor.chat.client.remoteList
import io.ktor.chat.client.remoteListWithUpdates
import io.ktor.chat.components.RemoteLoader
import io.ktor.chat.messages.MessageInput
import io.ktor.chat.messages.MessageList
import io.ktor.chat.rooms.RoomHeader
import io.ktor.chat.rooms.RoomsMenu
import io.ktor.chat.settings.UserMenu
import io.ktor.chat.utils.Done
import io.ktor.chat.utils.Remote
import io.ktor.chat.vm.ChatViewModel
import io.ktor.chat.vm.VideoCallViewModel
import io.ktor.chat.vm.createViewModel
import kotlin.time.Clock

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(vm: ChatViewModel = createViewModel(), videoCallVM: VideoCallViewModel? = null) {
    var selectedRoom by remember { vm.room }
    val currentUser by remember { vm.loggedInUser }
    val roomsRemote: Remote<SnapshotStateList<Membership>> by vm.memberships.remoteListWithUpdates(
        predicate = { it.user.id == currentUser!!.id }
    )
    val messagesRemote: Remote<SnapshotStateList<Message>> by vm.messages.listInRoom(selectedRoom?.room)
    val smallScreen by remember { derivedStateOf { vm.screenSize.value.first < 1400 } }
    val scope = rememberCoroutineScope()

    // Track incoming call and which user is calling
    var showIncomingCallDialog by remember { mutableStateOf(false) }
    var callingRequest by remember { mutableStateOf<JoinCall?>(null) }

    LaunchedEffect(Unit) {
        videoCallVM?.callRequests?.collect { request ->
            callingRequest = request
            showIncomingCallDialog = true
        }
    }

    fun exisingRoomWithId(id: ULong): Membership? = when (roomsRemote) {
        is Done -> (roomsRemote as Done<SnapshotStateList<Membership>>).value.firstOrNull { it.room.id == id }
        else -> null
    }

    if (showIncomingCallDialog && callingRequest != null) {
        AlertDialog(
            onDismissRequest = {
                showIncomingCallDialog = false
                scope.launch {
                    videoCallVM?.rejectCall(callingRequest!!)
                }
            },
            title = { Text("Incoming Call") },
            text = { Text("${callingRequest!!.sender.name} is calling. Do you want to join?") },
            confirmButton = {
                Button(
                    onClick = {
                        showIncomingCallDialog = false
                        selectedRoom = exisingRoomWithId(callingRequest!!.roomId) ?: return@Button
                        scope.launch {
                            videoCallVM?.acceptCall(callingRequest!!)
                        }
                    }
                ) {
                    Text("Accept")
                }
            },
            dismissButton = {
                Button(
                    onClick = {
                        showIncomingCallDialog = false
                        scope.launch {
                            videoCallVM?.rejectCall(callingRequest!!)
                        }
                    }
                ) {
                    Text("Decline")
                }
            }
        )
    }


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
                selectedRoom = vm.memberships.create(
                    Membership(
                        user = currentUser!!,
                        room = joinedRoom,
                    )
                )
                videoCallVM?.reinitSession()
            },
            onCreate = { newRoomName ->
                vm.rooms.create(Room(newRoomName)).let { newRoom ->
                    selectedRoom = vm.memberships.create(
                        Membership(
                            user = currentUser!!,
                            room = newRoom,
                        )
                    )
                    videoCallVM?.reinitSession()
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
                    videoCallVM?.reinitSession()
                    selectedRoom = null
                },
                onUpdateRoom = {
                    vm.rooms.update(it)
                    selectedRoom = selectedRoom?.copy(room = it)
                },
                onDeleteRoom = {
                    vm.rooms.delete(it.id)
                    videoCallVM?.reinitSession()
                    selectedRoom = null
                },
                onCreate = { messageText ->
                    vm.messages.create(
                        Message(
                            author = currentUser!!,
                            created = Clock.System.now(),
                            room = selectedRoom!!.room.id,
                            text = messageText,
                        )
                    )
                },
                onVideoCallInitiated = {
                    videoCallVM?.initiateCall(selectedRoom!!.room.id)
                }
            )
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
    onVideoCallInitiated: (suspend () -> Unit)?
) {
    if (selectedRoom == null) {
        Box(modifier = Modifier.fillMaxWidth().fillMaxHeight().windowInsetsPadding(WindowInsets.safeDrawing)) {
            Text(modifier = Modifier.align(Alignment.Center), text = "Select a room to begin")
        }
        return
    }

    RemoteLoader(messagesRemote) { messages ->
        Box(modifier = Modifier.fillMaxWidth().fillMaxHeight().windowInsetsPadding(WindowInsets.safeDrawing)) {
            RoomHeader(
                modifier = Modifier.align(TopStart).height(50.dp),
                membership = selectedRoom,
                onLeaveRoom = onLeaveRoom,
                onUpdateRoom = onUpdateRoom,
                onDeleteRoom = onDeleteRoom,
                onVideoCallInitiated = onVideoCallInitiated
            )
            MessageList(modifier = Modifier.align(TopCenter).padding(top = 50.dp, bottom = 60.dp), messages)
            MessageInput(modifier = Modifier.align(BottomCenter).height(60.dp), onCreate)
        }
    }
}