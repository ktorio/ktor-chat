package ktor.chat.vm

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import io.ktor.client.webrtc.WebRTCClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import ktor.chat.client.CallSessionManager
import ktor.chat.client.CallSessionManagerImpl
import ktor.chat.client.SignalingClient

class VideoCallViewModel(
    rtcClient: WebRTCClient,
    val signalingClient: SignalingClient,
    private val callSessionManager: CallSessionManager = CallSessionManagerImpl(signalingClient, rtcClient)
) : ViewModel(), CallSessionManager by callSessionManager {

    var isInVideoCall = mutableStateOf(false)

    fun init(scope: CoroutineScope) {
        signalingClient.connect(scope)
        scope.launch { listenCommandsFlow() }
    }
}