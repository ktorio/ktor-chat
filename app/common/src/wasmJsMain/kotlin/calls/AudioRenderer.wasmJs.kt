package io.ktor.chat.calls

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import io.ktor.client.webrtc.*
import kotlinx.browser.document
import org.w3c.dom.HTMLAudioElement
import org.w3c.dom.mediacapture.MediaStream

/**
 * WasmJs implementation of AudioRenderer.
 * This composable plays WebRTC audio track on a WasmJs platform.
 *
 * @param audioTrack The audio track to play
 */
@Composable
actual fun AudioRenderer(
    audioTrack: WebRtcMedia.AudioTrack
) {
    fun getStream(): MediaStream {
        return MediaStream().apply { addTrack(audioTrack.getNative()) }
    }

    // Create an audio element and play the audio track
    DisposableEffect(audioTrack) {
        val audioElement = document.createElement("audio") as HTMLAudioElement
        audioElement.srcObject = getStream()
        audioElement.autoplay = true
        document.body?.appendChild(audioElement)

        println("WasmJs Audio track enabled")

        onDispose {
            document.body?.removeChild(audioElement)
            println("WasmJs Audio track disabled")
        }
    }
}
