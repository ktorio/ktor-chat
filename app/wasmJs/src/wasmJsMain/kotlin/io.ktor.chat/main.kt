package io.ktor.chat

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import io.ktor.chat.client.*
import io.ktor.chat.vm.*
import io.ktor.client.*
import io.ktor.client.webrtc.*
import io.ktor.client.webrtc.peer.*
import kotlinx.browser.document
import kotlinx.coroutines.await
import org.w3c.dom.mediacapture.MediaStream
import org.w3c.dom.mediacapture.MediaStreamConstraints
import org.w3c.dom.mediacapture.MediaTrackConstraints

fun WebRTCMedia.AudioTrackConstraints.toJSs(): MediaTrackConstraints {
    return MediaTrackConstraints(
        volume = volume?.toJsNumber(),
        latency = latency?.toJsNumber(),
        sampleRate = sampleRate?.toJsNumber(),
        sampleSize = sampleSize?.toJsNumber(),
        echoCancellation = echoCancellation?.toJsBoolean(),
        autoGainControl = autoGainControl?.toJsBoolean(),
        noiseSuppression = noiseSuppression?.toJsBoolean(),
        channelCount = channelCount?.toJsNumber(),
    )
}

object SuperMediaDevices : MediaTrackFactory {
    override suspend fun createAudioTrack(constraints: WebRTCMedia.AudioTrackConstraints): WebRTCMedia.AudioTrack {
        val streamConstrains = MediaStreamConstraints(audio = true.toJsBoolean())
        val mediaStream = navigator.mediaDevices.getUserMedia(streamConstrains).await<MediaStream>()
        return WasmJsAudioTrack(mediaStream.getAudioTracks()[0]!!, mediaStream)
    }

    override suspend fun createVideoTrack(constraints: WebRTCMedia.VideoTrackConstraints): WebRTCMedia.VideoTrack {
        val streamConstrains = MediaStreamConstraints(video = true.toJsBoolean())
        val mediaStream = navigator.mediaDevices.getUserMedia(streamConstrains).await<MediaStream>()
        return WasmJsVideoTrack(mediaStream.getVideoTracks()[0]!!, mediaStream)
    }
}

fun createVideoCallVm(http: () -> HttpClient): VideoCallViewModel {
    val rtcClient = WebRTCClient(JsWebRTC) {
        iceServers = listOf(WebRTC.IceServer(urls = "stun:stun.l.google.com:19302"))
        turnServers = listOf(
            WebRTC.IceServer(
                urls = "turn:global.turn.twilio.com:3478?transport=udp",
                username = "096b3c487f5f7d7ff1b36b6ee72ab8d7f282eac2e96801c64ae51459c7886c5e",
                credential = "Ic9K79BvzMz3K6SdqyPWZ6weWEwlNogis81AxV4FsVs="
            )
        )
        statsRefreshRate = 10_000
        remoteTracksReplay = 50
        iceCandidatesReplay = 50
        mediaTrackFactory = SuperMediaDevices
    }
    return VideoCallViewModel(rtcClient, HttpSignalingClient(http))
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