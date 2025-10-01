package io.ktor.chat.calls

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import io.ktor.client.webrtc.*
import kotlinx.cinterop.ExperimentalForeignApi

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun AudioRenderer(
    audioTrack: WebRtcMedia.AudioTrack
) {
    DisposableEffect(audioTrack) {
        audioTrack.enable(true)
        println("Audio track enabled: ${audioTrack.id}")

        onDispose {
            audioTrack.enable(false)
        }
    }
}
