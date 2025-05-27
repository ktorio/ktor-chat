package ktor.chat.calls

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.ktor.client.webrtc.WebRTCMedia

/**
 * Renders a video track in a Compose UI.
 */
@Composable
expect fun VideoRenderer(
    videoTrack: WebRTCMedia.VideoTrack,
    modifier: Modifier = Modifier
)