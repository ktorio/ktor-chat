package io.ktor.chat.calls

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import io.ktor.client.webrtc.*
import io.ktor.client.webrtc.media.*
import org.webrtc.EglBase
import org.webrtc.RendererCommon
import org.webrtc.SurfaceViewRenderer
import org.webrtc.VideoTrack

object EglBaseProvider {
    var eglBase: EglBase? = null
}

/**
 * Android implementation of VideoRenderer.
 * This composable renders WebRTC video track on an Android platform.
 *
 * @param videoTrack The video track to render
 * @param modifier Modifier for styling
 */
@Composable
actual fun VideoRenderer(
    videoTrack: WebRtcMedia.VideoTrack,
    modifier: Modifier
) {
    // Get the native android video track from WebRtcMedia.VideoTrack
    val nativeVideoTrack by remember(videoTrack) {
        mutableStateOf(videoTrack.getNative() as VideoTrack)
    }
    var renderer by remember { mutableStateOf<SurfaceViewRenderer?>(null) }

    val lifecycleEventObserver = remember(renderer, videoTrack) {
        LifecycleEventObserver { _, event ->
            val r = renderer ?: return@LifecycleEventObserver
            when (event) {
                Lifecycle.Event.ON_RESUME -> {
                    val eglContext = requireNotNull(EglBaseProvider.eglBase?.eglBaseContext) {
                        "EglBase is not initialized"
                    }
                    r.init(eglContext, null)
                    nativeVideoTrack.addSink(r)
                    nativeVideoTrack.state()
                }

                Lifecycle.Event.ON_PAUSE -> {
                    nativeVideoTrack.removeSink(r)
                    r.release()
                }

                else -> null  // ignore other events
            }
        }
    }

    val lifecycle = LocalLifecycleOwner.current.lifecycle
    DisposableEffect(lifecycle, lifecycleEventObserver) {
        lifecycle.addObserver(lifecycleEventObserver)

        onDispose {
            renderer?.let { r ->
                nativeVideoTrack.removeSink(r)
                r.release()
            }
            lifecycle.removeObserver(lifecycleEventObserver)
        }
    }

    AndroidView(
        modifier = modifier,
        factory = { context ->
            SurfaceViewRenderer(context).apply {
                setScalingType(
                    RendererCommon.ScalingType.SCALE_ASPECT_BALANCED,
                    RendererCommon.ScalingType.SCALE_ASPECT_FIT
                )
                renderer = this
            }
        },
    )
}
