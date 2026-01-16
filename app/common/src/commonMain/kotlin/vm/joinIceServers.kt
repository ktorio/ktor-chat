package io.ktor.chat.vm

import io.ktor.client.webrtc.*

fun joinIceServers(
    stunUrl: String?, stunUsername: String?, stunCredential: String?,
    turnUrl: String?, turnUsername: String?, turnCredential: String?
): List<WebRtc.IceServer> {
    val configuredIceServers = mutableListOf<WebRtc.IceServer>()
    if (stunUrl != null) {
        configuredIceServers.add(
            WebRtc.IceServer(
                urls = listOf(stunUrl),
                username = stunUsername,
                credential = stunCredential
            )
        )
    }
    if (turnUrl != null) {
        configuredIceServers.add(
            WebRtc.IceServer(
                urls = listOf(turnUrl),
                username = turnUsername,
                credential = turnCredential
            )
        )
    }
    return configuredIceServers
}