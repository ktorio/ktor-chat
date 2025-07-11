package io.ktor.chat

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.core.content.ContextCompat
import io.ktor.chat.app.*
import io.ktor.chat.calls.*
import io.ktor.chat.client.*
import io.ktor.chat.vm.*
import io.ktor.client.*
import io.ktor.client.webrtc.*
import io.ktor.client.webrtc.media.*
import org.webrtc.EglBase

fun createVideoCallVm(ctx: Context, http: () -> HttpClient): VideoCallViewModel {
    val egbBase = EglBase.create()
    EglBaseProvider.eglBase = egbBase

    val rtcClient = WebRtcClient(AndroidWebRtc) {
        defaultConnectionConfig = {
            iceServers = joinIceServers(
                BuildKonfig.STUN_URL,
                BuildKonfig.STUN_USERNAME,
                BuildKonfig.STUN_CREDENTIAL,
                BuildKonfig.TURN_URL,
                BuildKonfig.TURN_USERNAME,
                BuildKonfig.TURN_CREDENTIAL
            )
            statsRefreshRate = 10_000
            remoteTracksReplay = 10
        }
        mediaTrackFactory = AndroidMediaDevices(ctx, egbBase)
    }

    return VideoCallViewModel(rtcClient, HttpSignalingClient(http))
}

class MainActivity : ComponentActivity() {
    // Required permissions for video calls
    private val requiredPermissions = arrayOf(
        Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO
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
        enableEdgeToEdge()

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