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
    // Get the native android audio track from WebRtcMedia.AudioTrack
    val nativeAudioTrack by remember(audioTrack) {
        mutableStateOf(audioTrack.getNative())
    }

    // Set the audio track to enable to play it
    DisposableEffect(nativeAudioTrack) {
        nativeAudioTrack.setEnabled(true)
        println("Audio track enabled: ${nativeAudioTrack.id()}")
        
        onDispose {
            if (nativeAudioTrack.isDisposed) {
                return@onDispose
            }
            nativeAudioTrack.setEnabled(false)
        }
    }
}