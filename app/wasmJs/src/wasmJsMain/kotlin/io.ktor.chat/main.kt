package io.ktor.chat

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import io.ktor.chat.app.*
import io.ktor.chat.client.*
import io.ktor.chat.vm.*
import io.ktor.client.*
import io.ktor.client.webrtc.*
import io.ktor.utils.io.ExperimentalKtorApi
import kotlinx.browser.document
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalKtorApi::class)
fun createVideoCallVm(http: () -> HttpClient): VideoCallViewModel {
    val rtcClient = WebRtcClient(JsWebRtc) {
        defaultConnectionConfig = {
            iceServers = joinIceServers(
                BuildKonfig.STUN_URL,
                BuildKonfig.STUN_USERNAME,
                BuildKonfig.STUN_CREDENTIAL,
                BuildKonfig.TURN_URL,
                BuildKonfig.TURN_USERNAME,
                BuildKonfig.TURN_CREDENTIAL
            )
            statsRefreshRate = 10.seconds
            remoteTracksReplay = 50
            iceCandidatesReplay = 50
        }
    }
    val enableSecureConnection = BuildKonfig.SERVER_URL.startsWith("https")
    val signalingClient = HttpSignalingClient(http = http, enableSecureConnection = enableSecureConnection)
    return VideoCallViewModel(rtcClient, signalingClient)
}

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    ComposeViewport(document.body!!) {
        val chatClient = HttpChatClient()
        val chatVm = createViewModel(chatClient)
        val videoCallVm = createVideoCallVm { chatClient.getHttp() }
        ChatApplication(chatVm, videoCallVm)
    }
}