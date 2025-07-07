package io.ktor.chat.client

import io.ktor.chat.SignalingCommand
import io.ktor.client.*
import io.ktor.client.plugins.websocket.receiveDeserialized
import io.ktor.client.plugins.websocket.sendSerialized
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.client.plugins.websocket.wss
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.http.HttpHeaders
import io.ktor.http.headers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch

class HttpSignalingClient(
    private val http: () -> HttpClient,
    private val reconnectCount: Int = 10
) : SignalingClient {
    private val _signalingCommandFlow = MutableSharedFlow<SignalingCommand>()
    override val signalingCommandFlow: SharedFlow<SignalingCommand> = _signalingCommandFlow

    private val _outgoingCommands = MutableSharedFlow<SignalingCommand>()

    override suspend fun sendCommand(command: SignalingCommand) {
        println("Sending command: $command")
        _outgoingCommands.emit(command)
    }

    override fun connect(scope: CoroutineScope, token: String): Job = scope.launch {
        var reconnections = 0
        while (reconnections < reconnectCount) runCatching {
            http().wss(path = "call", request = {
                parameter(HttpHeaders.Authorization, "Bearer $token")
            }) {
                val sendJob = scope.launch {
                    _outgoingCommands.collect { sendSerialized(it) }
                }
                println("Listening for commands on WebSocket")
                runCatching {
                    while (true) {
                        val command = receiveDeserialized<SignalingCommand>()
                        println("Received command: $command")
                        _signalingCommandFlow.emit(command)
                    }
                }.onFailure { it.printStackTrace() }

                sendJob.takeIf { it.isActive }?.cancel()
            }
            delay(1000)
        }.onFailure {
            it.printStackTrace()
            reconnections++
        }
    }
}
