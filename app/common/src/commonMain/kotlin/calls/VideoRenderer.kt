package io.ktor.chat.calls

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.ktor.client.webrtc.WebRtcMedia

/**
 * Renders a video track in a Compose UI.
 */
@Composable
expect fun VideoRenderer(
    videoTrack: WebRtcMedia.VideoTrack,
    modifier: Modifier = Modifier
)