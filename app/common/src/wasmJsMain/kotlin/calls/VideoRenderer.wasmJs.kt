package io.ktor.chat.calls

import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.viewinterop.WebElementView
import io.ktor.client.webrtc.*
import kotlinx.browser.document
import org.w3c.dom.HTMLVideoElement
import org.w3c.dom.mediacapture.MediaStream
import org.w3c.dom.mediacapture.MediaStreamTrack

@OptIn(ExperimentalComposeUiApi::class)
@Composable
actual fun VideoRenderer(
    videoTrack: WebRtcMedia.VideoTrack,
    modifier: androidx.compose.ui.Modifier
) {
    fun getStream(): MediaStream {
        return MediaStream().apply {
            // cast `kotlin-wrappers` to `org.w3c.dom`, because WebElementView depends on it.
            @Suppress("CAST_NEVER_SUCCEEDS")
            addTrack(videoTrack.getNative() as MediaStreamTrack)
        }
    }

    WebElementView(
        factory = {
            (document.createElement("video") as HTMLVideoElement).apply {
                style.objectFit = "cover"
                srcObject = getStream()
                autoplay = true
            }
        },
        modifier = modifier,
        update = { video ->
            video.srcObject = getStream()
        }
    )
}