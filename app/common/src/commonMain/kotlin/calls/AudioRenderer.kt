package io.ktor.chat.calls

import androidx.compose.runtime.Composable
import io.ktor.client.webrtc.WebRtcMedia

/**
 * Renders an audio track.
 */
@Composable
expect fun AudioRenderer(
    audioTrack: WebRtcMedia.AudioTrack
)