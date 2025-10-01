package io.ktor.chat.calls

import WebRTC.RTCMTLVideoView
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.viewinterop.UIKitView
import io.ktor.client.webrtc.*
import io.ktor.client.webrtc.media.getNative
import kotlinx.cinterop.ExperimentalForeignApi
import platform.UIKit.UIViewContentMode

@OptIn(ExperimentalComposeUiApi::class, ExperimentalForeignApi::class)
@Composable
actual fun VideoRenderer(
    videoTrack: WebRtcMedia.VideoTrack,
    modifier: androidx.compose.ui.Modifier
) {
    UIKitView(
        factory = {
            RTCMTLVideoView().apply {
                videoContentMode = UIViewContentMode.UIViewContentModeScaleAspectFit
                videoTrack.getNative().addRenderer(this)
            }
        },
        modifier = modifier,
        onRelease = {
            videoTrack.getNative().removeRenderer(it)
        },
    )
}