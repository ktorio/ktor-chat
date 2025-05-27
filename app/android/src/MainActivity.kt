package io.ktor.chat

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.core.content.ContextCompat
import io.ktor.client.HttpClient
import io.ktor.client.webrtc.WebRTC
import io.ktor.client.webrtc.WebRTCClient
import io.ktor.client.webrtc.engine.AndroidMediaDevices
import io.ktor.client.webrtc.engine.AndroidWebRTC
import ktor.chat.ChatApplication
import ktor.chat.calls.EglBaseProvider
import ktor.chat.client.HttpChatClient
import ktor.chat.client.HttpSignalingClient
import ktor.chat.vm.VideoCallViewModel
import ktor.chat.vm.createViewModel
import org.webrtc.EglBase

fun createVideoCallVm(ctx: Context, http: () -> HttpClient): VideoCallViewModel {
    val egbBase = EglBase.create()
    EglBaseProvider.eglBase = egbBase

    val rtcClient = WebRTCClient(AndroidWebRTC) {
        iceServers = listOf(WebRTC.IceServer(urls = "stun:stun.l.google.com:19302", username = "", credential = ""))
        turnServers = listOf(WebRTC.IceServer(urls = "turn:10.0.2.2:3478", username = "user", credential = "pass"))
        statsRefreshRate = 10_000
        remoteTracksReplay = 10
        mediaTrackFactory = AndroidMediaDevices(ctx,egbBase)
    }

    return VideoCallViewModel(rtcClient, HttpSignalingClient(http))
}

class MainActivity : AppCompatActivity() {
    // Required permissions for video calls
    private val requiredPermissions = arrayOf(
        Manifest.permission.CAMERA,
        Manifest.permission.RECORD_AUDIO
    )

    // State to track if permissions are granted
    private val permissionsGranted = mutableStateOf(false)

    // Permission request launcher
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        // Check if all required permissions are granted
        permissionsGranted.value = permissions.entries.all { it.value }
    }

    private fun checkAndRequestPermissions() {
        val permissionsToRequest = requiredPermissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }.toTypedArray()

        // Request the permissions
        requestPermissionLauncher.launch(permissionsToRequest)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Check and request permissions early
        checkAndRequestPermissions()

        setContent {
            val chatClient = HttpChatClient()
            val chatVm = createViewModel(chatClient)
            val videoCallVm = createVideoCallVm(applicationContext) { chatClient.getHttp() }

            // Use LaunchedEffect to check permissions status when the app starts
            LaunchedEffect(Unit) {
                checkAndRequestPermissions()
            }

            ChatApplication(chatVm, videoCallVm)
        }
    }
}