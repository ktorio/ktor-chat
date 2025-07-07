package io.ktor.chat.calls

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.ktor.client.webrtc.*

/**
 * Displays a floating video renderer with username overlay.
 *
 * @param videoTrack The video track to render
 * @param userName The name to display for this video (default is empty)
 * @param modifier Modifier for styling
 */
@Composable
fun FloatingVideoRenderer(
    videoTrack: WebRTCMedia.VideoTrack,
    modifier: Modifier = Modifier,
    userName: String = "",
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Video card
        Card(
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.weight(1f)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                // Video track rendering
                VideoRenderer(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(16.dp)),
                    videoTrack = videoTrack
                )
            }
        }

        // Username below the video
        if (userName.isNotEmpty()) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = userName,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
            )
        }
    }

}