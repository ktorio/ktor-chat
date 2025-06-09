package io.ktor.chat.client

import io.ktor.chat.SignalingCommand
import io.ktor.client.*
import io.ktor.client.plugins.websocket.receiveDeserialized
import io.ktor.client.plugins.websocket.sendSerialized
import io.ktor.client.plugins.websocket.webSocket
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch

class HttpSignalingClient(private val http: () -> HttpClient) : SignalingClient {
    private val _signalingCommandFlow = MutableSharedFlow<SignalingCommand>()
    override val signalingCommandFlow: SharedFlow<SignalingCommand> = _signalingCommandFlow

    private val _outgoingCommands = MutableSharedFlow<SignalingCommand>()

    override suspend fun sendCommand(command: SignalingCommand) {
        println("Sending command: $command")
        _outgoingCommands.emit(command)
    }

    override fun connect(scope: CoroutineScope): Job = scope.launch {
        while (true) {
            http().webSocket(path = "call") {
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
                }

                sendJob.takeIf { it.isActive }?.cancel()
            }
        }
    }
}
