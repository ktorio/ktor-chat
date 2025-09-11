package io.ktor.chat.calls

import androidx.compose.runtime.Composable
import io.ktor.client.webrtc.*

/**
 * JVM implementation of AudioRenderer.
 * This composable plays WebRTC audio track on a JVM platform.
 *
 * @param audioTrack The audio track to play
 */
@Composable
actual fun AudioRenderer(
    audioTrack: WebRtcMedia.AudioTrack
) {
    TODO("Add example for desktop platform when Engine is added.")
}