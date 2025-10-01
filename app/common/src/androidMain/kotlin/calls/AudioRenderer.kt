package io.ktor.chat.calls

import androidx.compose.runtime.*
import io.ktor.client.webrtc.*
import io.ktor.client.webrtc.media.getNative

/**
 * Android implementation of AudioRenderer.
 * This composable plays WebRTC audio track on an Android platform.
 *
 * @param audioTrack The audio track to play
 */
@Composable
actual fun AudioRenderer(
    audioTrack: WebRtcMedia.AudioTrack
) {
    // Set the audio track to enable to play it
    DisposableEffect(audioTrack) {
        audioTrack.enable(true)
        println("Audio track enabled: ${audioTrack.id}")
        
        onDispose {
            if (audioTrack.getNative().isDisposed) {
                return@onDispose
            }
            audioTrack.enable(false)
        }
    }
}