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

    // for now, we only display video, but audio is also recorded and sent
    val remoteVideoTracks by vm.remoteVideoTracks.collectAsState()
    val localVideoTrack by vm.localVideoTrack.collectAsState()

    val connectedUsersCnt by vm.connectedUsersCount.collectAsState()
    val scope = rememberCoroutineScope()


    LaunchedEffect(selectedRoom, connectedUsersCnt) {
        println("Connected users: $connectedUsersCnt")
        requireNotNull(selectedRoom)
        vm.setupRoomCall(this)
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
            modifier = Modifier.fillMaxSize().padding(top=50.dp, bottom = 80.dp) // Space for controls
        ) {
            if (remoteVideoTracks.isEmpty()) {
                Text(
                    text = "Waiting for others to join",
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                // Create a responsive grid layout
                FlowRow(
                    modifier = Modifier.fillMaxSize().padding(8.dp),
                    maxItemsInEachRow = 2, // Adjust based on screen size
                ) {
                    // Add remote video tracks
                    remoteVideoTracks.forEach { (interlocutorName, remoteVideoTrack) ->
                        FloatingVideoRenderer(
                            videoTrack = remoteVideoTrack,
                            userName = interlocutorName,
                            modifier = Modifier
                                .weight(1f)
                                .width(150.dp)
                                .height(200.dp)
                                .padding(4.dp)
                        )
                    }

                    // Add local video if available and enabled
                    localVideoTrack?.let {
                        if (callMediaState.isCameraEnabled) {
                            FloatingVideoRenderer(
                                videoTrack = it,
                                userName = "You",
                                modifier = Modifier
                                    .weight(1f)
                                    .width(150.dp)
                                    .height(200.dp)
                                    .padding(4.dp)

                            )
                        }
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