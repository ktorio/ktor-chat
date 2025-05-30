package io.ktor.chat.calls

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import io.ktor.client.webrtc.*
import org.webrtc.EglBase
import org.webrtc.RendererCommon
import org.webrtc.SurfaceViewRenderer

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
    videoTrack: WebRTCMedia.VideoTrack,
    modifier: Modifier
) {
    // Get the native android video track from WebRTCMedia.VideoTrack
    val nativeVideoTrack by remember(videoTrack) {
        mutableStateOf(videoTrack.getNative() as org.webrtc.VideoTrack)
    }
    var renderer by remember { mutableStateOf<SurfaceViewRenderer?>(null) }

    val lifecycleEventObserver = remember(renderer, videoTrack) {
        LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> {
                    renderer?.also {
                        val eglContext = requireNotNull(EglBaseProvider.eglBase?.eglBaseContext) {
                            "EglBase is not initialized"
                        }
                        it.init(eglContext, null)
                        nativeVideoTrack.addSink(it)
                        nativeVideoTrack.state()
                    }
                }

                Lifecycle.Event.ON_PAUSE -> {
                    renderer?.also { nativeVideoTrack.removeSink(it) }
                    renderer?.release()
                }

                else -> {
                    // ignore other events
                }
            }
        }
    }

    val lifecycle = LocalLifecycleOwner.current.lifecycle
    DisposableEffect(lifecycle, lifecycleEventObserver) {
        lifecycle.addObserver(lifecycleEventObserver)

        onDispose {
            renderer?.let { nativeVideoTrack.removeSink(it) }
            renderer?.release()
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
