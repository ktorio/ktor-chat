package io.ktor.chat.calls

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import io.ktor.client.webrtc.WebRtcMedia

/**
 * JVM implementation of VideoRenderer.
 * Since video calls are not fully supported in JVM applications yet,
 * this implementation displays a message to the user.
 *
 * @param videoTrack The video track to render (not used in JVM implementation)
 * @param modifier Modifier for styling
 */
@Composable
actual fun VideoRenderer(
    videoTrack: WebRtcMedia.VideoTrack,
    modifier: Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.DarkGray),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Video calls are not supported for JVM application yet...",
            color = Color.White,
            textAlign = TextAlign.Center
        )
    }
}
