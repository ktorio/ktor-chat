package io.ktor.chat.vm

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import io.ktor.chat.JoinCall
import io.ktor.chat.client.CallSessionManager
import io.ktor.chat.client.CallSessionManagerImpl
import io.ktor.chat.client.SignalingClient
import io.ktor.client.webrtc.WebRTCClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

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

    override suspend fun initiateCall(roomId: Long) {
        callSessionManager.initiateCall(roomId)
        isInVideoCall.value = true
    }

    override suspend fun acceptCall(request: JoinCall) {
        callSessionManager.acceptCall(request)
        isInVideoCall.value = true
    }

    suspend fun leaveCall() {
        isInVideoCall.value = false
        callSessionManager.disconnect()
    }
}