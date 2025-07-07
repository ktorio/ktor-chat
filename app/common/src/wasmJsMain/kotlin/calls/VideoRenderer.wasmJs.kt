package io.ktor.chat.calls

import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.viewinterop.WebElementView
import io.ktor.client.webrtc.WasmJsVideoTrack
import io.ktor.client.webrtc.WebRTCMedia
import kotlinx.browser.document
import org.w3c.dom.HTMLVideoElement
import org.w3c.dom.mediacapture.MediaStream

@OptIn(ExperimentalComposeUiApi::class)
@Composable
actual fun VideoRenderer(
    videoTrack: WebRTCMedia.VideoTrack,
    modifier: androidx.compose.ui.Modifier
) {
    fun getStream(): MediaStream {
        val track = (videoTrack as WasmJsVideoTrack).nativeTrack
        return MediaStream().apply { addTrack(track) }
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