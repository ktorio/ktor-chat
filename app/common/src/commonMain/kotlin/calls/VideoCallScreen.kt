package io.ktor.chat.calls

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.TopStart
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.ktor.chat.rooms.RoomHeader
import io.ktor.chat.vm.ChatViewModel
import io.ktor.chat.vm.VideoCallViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun VideoCallScreen(vm: VideoCallViewModel, chatVm: ChatViewModel) {
    val selectedRoom by remember { chatVm.room }

    var callMediaState by remember { mutableStateOf(CallMediaState()) }

    val remoteVideoTracks by vm.remoteVideoTracks.collectAsState()
    val remoteAudioTracks by vm.remoteAudioTracks.collectAsState()
    val localVideoTrack by vm.localVideoTrack.collectAsState()

    val connectedUsersCnt by vm.connectedUsersCount.collectAsState()
    val scope = rememberCoroutineScope()


    LaunchedEffect(selectedRoom, connectedUsersCnt) {
        println("Connected users: $connectedUsersCnt")
        requireNotNull(selectedRoom)
        vm.setupRoomCall(this)
    }

    // Render all remote audio tracks to ensure they are played
    remoteAudioTracks.forEach { (_, audioTrack) ->
        AudioRenderer(audioTrack = audioTrack)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        RoomHeader(
            modifier = Modifier.align(TopStart).height(50.dp),
            membership = selectedRoom!!,
            onLeaveRoom = null,
            onUpdateRoom = null,
            onDeleteRoom = null,
            onVideoCallInitiated = null
        )

        // Main content area for video tracks
        Box(
            modifier = Modifier.fillMaxSize().padding(top = 50.dp, bottom = 80.dp) // Space for controls
        ) {
            val rowSize = 2
            val padding = 5.dp
            val cellSize = 200.dp

            if (remoteVideoTracks.isEmpty()) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    if (localVideoTrack != null) {
                        FloatingVideoRenderer(
                            videoTrack = localVideoTrack!!,
                            userName = "You",
                            modifier = Modifier
                                .width(cellSize)
                                .height(cellSize)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(text = "Waiting for others to join")
                }

            } else {
                val maxWidth = cellSize * rowSize + padding * (rowSize - 1)
                // Create a responsive grid layout
                FlowRow(
                    modifier = Modifier
                        .padding(8.dp)
                        .width(maxWidth)
                        .align(Alignment.Center),
                    maxItemsInEachRow = 2, // Adjust based on screen size
                ) {
                    val tracks = if (callMediaState.isCameraEnabled && localVideoTrack != null) {
                        remoteVideoTracks + ("You" to localVideoTrack!!)
                    } else remoteVideoTracks

                    (tracks).forEach { (interlocutorName, remoteVideoTrack) ->
                        FloatingVideoRenderer(
                            videoTrack = remoteVideoTrack,
                            userName = interlocutorName,
                            modifier = Modifier
                                .weight(1f)
                                .width(cellSize)
                                .height(cellSize)
                                .padding(padding)
                        )
                    }
                }
            }
        }

        // Call controls at the bottom
        VideoCallControls(
            modifier = Modifier.fillMaxWidth().align(Alignment.BottomCenter).padding(bottom = 16.dp),
            callMediaState = callMediaState,
            onCallAction = {
                when (it) {
                    is CallAction.ToggleMicrophone -> {
                        val enabled = !callMediaState.isMicrophoneEnabled
                        callMediaState = callMediaState.copy(isMicrophoneEnabled = enabled)
                        vm.enableMicrophone(enabled)
                    }

                    is CallAction.ToggleCamera -> {
                        val enabled = !callMediaState.isCameraEnabled
                        callMediaState = callMediaState.copy(isCameraEnabled = enabled)
                        vm.enableCamera(enabled)
                    }

                    CallAction.EndCall -> {
                        scope.launch { vm.leaveCall() }
                    }
                }
            })
    }
}
