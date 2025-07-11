package io.ktor.chat.client

import io.ktor.chat.*
import io.ktor.client.*
import io.ktor.client.plugins.websocket.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch

class HttpSignalingClient(
    private val http: () -> HttpClient,
    private val reconnectCount: Int = 5,
    private val enableSecureConnection: Boolean = false,
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
            connectOnce(scope, token)
            delay(1000)
        }.onFailure {
            it.printStackTrace()
            reconnections++
        }
    }

    private suspend fun connectOnce(scope: CoroutineScope, token: String) {
        if (enableSecureConnection) {
            http().wss(path = "call", request = { withAuth(token) }) { onConnect(scope) }
        } else {
            http().ws("call", request = { withAuth(token) }) { onConnect(scope) }
        }
    }

    private fun HttpRequestBuilder.withAuth(token: String) {
        parameter(HttpHeaders.Authorization, "Bearer $token")
    }

    private suspend fun DefaultClientWebSocketSession.onConnect(scope: CoroutineScope) {
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
}
