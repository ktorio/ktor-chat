// MainViewController is located here to generate only one iOS framework
// Generating another iOS framework for one file makes little sense

package io.ktor.chat

import androidx.compose.ui.window.ComposeUIViewController
import io.ktor.chat.app.*
import io.ktor.chat.client.*
import io.ktor.chat.vm.*
import io.ktor.client.*
import io.ktor.client.webrtc.*
import io.ktor.utils.io.*
import platform.AVFoundation.*
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalKtorApi::class)
fun createVideoCallVm(http: () -> HttpClient): VideoCallViewModel {
    val rtcClient = WebRtcClient(IosWebRtc) {
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
            remoteTracksReplay = 10
        }
    }

    return VideoCallViewModel(rtcClient, HttpSignalingClient(http))
}

fun askForPermission(mediaType: AVMediaType) {
    val status = AVCaptureDevice.authorizationStatusForMediaType(mediaType)
    if (status == AVAuthorizationStatusNotDetermined) {
        AVCaptureDevice.requestAccessForMediaType(mediaType) {
            println("Granted access for $mediaType: $it")
        }
    }
}

fun MainViewController() = ComposeUIViewController {
    val chatClient = HttpChatClient()
    val chatVm = createViewModel(chatClient)
    val videoCallVm = createVideoCallVm { chatClient.getHttp() }

    askForPermission(mediaType = AVMediaTypeVideo)
    askForPermission(mediaType = AVMediaTypeAudio)

    ChatApplication(chatVm, videoCallVm)
}