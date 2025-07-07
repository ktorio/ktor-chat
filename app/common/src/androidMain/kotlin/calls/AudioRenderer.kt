package io.ktor.chat.calls

import androidx.compose.runtime.*
import io.ktor.client.webrtc.*
import org.webrtc.AudioTrack

/**
 * Android implementation of AudioRenderer.
 * This composable plays WebRTC audio track on an Android platform.
 *
 * @param audioTrack The audio track to play
 */
@Composable
actual fun AudioRenderer(
    audioTrack: WebRTCMedia.AudioTrack
) {
    // Get the native android audio track from WebRTCMedia.AudioTrack
    val nativeAudioTrack by remember(audioTrack) {
        mutableStateOf(audioTrack.getNative() as AudioTrack)
    }

    // Set the audio track to enabled to play it
    DisposableEffect(nativeAudioTrack) {
        nativeAudioTrack.setEnabled(true)
        println("Audio track enabled: ${nativeAudioTrack.id()}")
        
        onDispose {
            nativeAudioTrack.setEnabled(false)
            println("Audio track disabled: ${nativeAudioTrack.id()}")
        }
    }
}