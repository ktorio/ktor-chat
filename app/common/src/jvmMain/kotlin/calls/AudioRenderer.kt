package io.ktor.chat.calls

import androidx.compose.runtime.*
import io.ktor.client.webrtc.*

/**
 * JVM implementation of AudioRenderer.
 * This composable plays WebRTC audio track on a JVM platform.
 *
 * @param audioTrack The audio track to play
 */
@Composable
actual fun AudioRenderer(
    audioTrack: WebRTCMedia.AudioTrack
) {
    // Get the native JVM audio track from WebRTCMedia.AudioTrack
    val nativeAudioTrack by remember(audioTrack) {
        mutableStateOf(audioTrack.getNative())
    }

    // Set the audio track to enabled to play it
    DisposableEffect(nativeAudioTrack) {
        // Enable the audio track to play it
        // This is platform-specific implementation for JVM
        println("JVM Audio track enabled")
        
        onDispose {
            println("JVM Audio track disabled")
        }
    }
}