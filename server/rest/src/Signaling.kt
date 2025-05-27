package io.ktor.chat

import DirectedCommand
import SignalingCommand
import IceExchange
import JoinCall
import LeaveCall
import OngoingCall
import PickUpCall
import SdpAnswer
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.plugins.*
import io.ktor.server.plugins.di.dependencies
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

object SessionManager {

    data class SignalingEvent(val command: SignalingCommand, val recipientId: Long)

    private val sessionManagerScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val mutex = Mutex()

    // Set of room IDs that are in call
    private val roomsInCall = mutableSetOf<Long>()

    private val roomClients = mutableMapOf<Long, MutableSet<Long>>()

    // Flow for signaling commands, with room ID
    private val _signalingCommands = MutableSharedFlow<SignalingEvent>()
    val signalingCommands: SharedFlow<SignalingEvent> = _signalingCommands.asSharedFlow()

    fun onClientConnected(clientId: Long, roomId: Long) {
        sessionManagerScope.launch {
            mutex.withLock {
                val currentRoomClients = roomClients.getOrPut(roomId) { mutableSetOf() }
                currentRoomClients.add(clientId)

                if (roomsInCall.contains(roomId)) {
                    _signalingCommands.emit(SignalingEvent(command = OngoingCall(roomId), recipientId = clientId))
                }
            }
        }
    }

    fun onCommand(client: User, command: SignalingCommand) {
        require(roomClients[command.roomId]?.contains(client.id) == true) {
            "Client ${client.id} is not in room ${command.roomId}"
        }

        when (command) {
            is JoinCall -> handleJoin(client, command)
            is SdpAnswer -> forwardCommand(client, command)
            is PickUpCall -> forwardCommand(client, command)
            is IceExchange -> forwardCommand(client, command)
            is LeaveCall -> onClientLeft(client, command, disconnected = false)
            is OngoingCall -> error("Client should not send ongoing call command")
        }
    }

    private fun broadcast(senderId: Long, command: SignalingCommand) {
        val allClients = roomClients[command.roomId] ?: error("Room ${command.roomId} not found")
        sessionManagerScope.launch {
            for (recipientId in allClients) {
                if (recipientId == senderId) continue
                _signalingCommands.emit(SignalingEvent(command, recipientId))
            }
        }
    }

    private fun sendTo(senderId: Long, recipientId: Long, command: SignalingCommand) {
        require(senderId != recipientId) {
            "Sender and recipient cannot be the same"
        }
        require(roomClients[command.roomId]?.contains(recipientId) == true) {
            "Client $recipientId is not in room ${command.roomId}"
        }
        sessionManagerScope.launch {
            _signalingCommands.emit(SignalingEvent(command, recipientId))
        }
    }

    private fun handleJoin(client: User, command: JoinCall) {
        require(command.sender == client) { "Sender ID doesn't match" }
        roomsInCall.add(command.roomId)
        broadcast(client.id, command)
    }

    private fun forwardCommand(client: User, command: DirectedCommand) {
        // Send an answer to all clients in the room except the sender
        require(command.sender == client) { "Sender ID doesn't match" }
        sendTo(client.id, command.recipientId, command)
    }

    fun onClientLeft(client: User, command: LeaveCall, disconnected: Boolean) {
        require(command.sender == client) { "Sender ID doesn't match" }
        if (roomClients[command.roomId]?.contains(client.id) != true) {
            return
        }

        broadcast(client.id, command)
        if (!disconnected) {
            return
        }
        sessionManagerScope.launch {
            mutex.withLock {
                roomClients[command.roomId]?.remove(client.id)
            }
        }
    }
}

fun Application.signaling() {
    val memberships: Repository<Membership, Long> by dependencies

    suspend fun userRooms(userId: Long): List<Long> {
        val query = MapQuery { this["user"] = userId }
        return memberships.list(query).map { it.room.id }
    }

    routing {
        authenticate {
            webSocket("call") {
                environment.log.info("SSE subscribe ${call.request.path()}")
                val user = call.principal<ChatPrincipal>()?.user ?: throw BadRequestException("Bad token")

                val startJob = launch {
                    for (roomId in userRooms(user.id)) {
                        SessionManager.onClientConnected(user.id, roomId)
                    }
                }

                // Listen for commands from all user rooms
                val listenCommandsJob = launch {
                    SessionManager.signalingCommands
                        .filter { it.recipientId == user.id }
                        .collect { sendSerialized(it.command) }
                }

                runCatching {
                    while (true) {
                        val command = receiveDeserialized<SignalingCommand>()
                        SessionManager.onCommand(client = user, command)
                        call.respond(HttpStatusCode.OK)
                    }
                }.onFailure { it.printStackTrace() }

                coroutineContext.job.invokeOnCompletion {
                    environment.log.info("Client ${user.name} disconnected")
                    launch {
                        userRooms(user.id).forEach { roomId ->
                            // will skip if the client is already disconnected
                            SessionManager.onClientLeft(user, LeaveCall(roomId, user), disconnected = true)
                        }
                    }
                    startJob.takeIf { it.isActive }?.cancel()
                    listenCommandsJob.cancel()
                }
            }
        }
    }
}
