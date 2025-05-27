package ktor.chat.calls

data class CallMediaState(
    val isMicrophoneEnabled: Boolean = true,
    val isCameraEnabled: Boolean = true
)