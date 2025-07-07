package io.ktor.chat.calls

import androidx.compose.runtime.*
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
    audioTrack: WebRTCMedia.AudioTrack
) {
    fun getStream(): MediaStream {
        val track = (audioTrack as WasmJsAudioTrack).nativeTrack
        return MediaStream().apply { addTrack(track) }
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
