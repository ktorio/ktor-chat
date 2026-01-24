package io.ktor.chat

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.plugins.*
import io.ktor.server.plugins.di.dependencies
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class SessionManager(
    private val memberships: Repository<Membership, ULong>
) {
    data class SignalingEvent(val command: SignalingCommand, val recipientId: ULong)

    private val sessionManagerScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val mutex = Mutex()

    // Set of room IDs that are in call
    private val roomsInCall = mutableSetOf<ULong>()

    private val roomClients = mutableMapOf<ULong, MutableSet<ULong>>()

    // Flow for signaling commands, with room ID
    private val _signalingCommands = MutableSharedFlow<SignalingEvent>(1)
    val signalingCommands: SharedFlow<SignalingEvent> = _signalingCommands.asSharedFlow()

    suspend fun userRooms(userId: ULong): List<ULong> {
        val query = MapQuery { this["user"] = userId }
        return memberships.list(query).map { it.room.id }
    }

    fun onClientConnected(client: User): Job = sessionManagerScope.launch {
        val rooms = userRooms(client.id)
        println("Client ${client.name} connected to rooms ${rooms.joinToString(", ")}")

        for (roomId in rooms) {
            mutex.withLock {
                roomClients.getOrPut(roomId) { mutableSetOf() }.add(client.id)
            }

            if (!roomsInCall.contains(roomId)) continue
            _signalingCommands.emit(SignalingEvent(command = OngoingCall(roomId), recipientId = client.id))
        }
    }

    fun onRoomCommand(client: User, command: RoomCommand) {
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

    private fun broadcast(senderId: ULong, command: RoomCommand): Job {
        val allClients = roomClients[command.roomId] ?: error("Room ${command.roomId} not found")
        return sessionManagerScope.launch {
            for (recipientId in allClients) {
                if (recipientId == senderId) continue
                _signalingCommands.emit(SignalingEvent(command, recipientId))
            }
        }
    }

    private fun sendTo(senderId: ULong, recipientId: ULong, command: RoomCommand) {
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
        sessionManagerScope.launch {
            broadcast(client.id, command).join()
            if (!disconnected) {
                return@launch
            }
            mutex.withLock {
                roomClients[command.roomId]?.remove(client.id)
                if (roomClients[command.roomId]?.isEmpty() == true) {
                    roomsInCall.remove(command.roomId)
                }
            }
        }
    }

    fun onClientDisconnected(client: User) = sessionManagerScope.launch {
        val rooms = userRooms(client.id)
        for (roomId in rooms) {
            onClientLeft(client, LeaveCall(roomId, client), disconnected = true)
        }
    }
}

fun Application.signaling() {
    val memberships: Repository<Membership, ULong> by dependencies
    val manager = SessionManager(memberships)

    routing {
        authenticate {
            webSocket("call") {
                environment.log.info("SSE subscribe ${call.request.path()}")
                val user = call.principal<ChatPrincipal>()?.user ?: throw BadRequestException("Bad token")

                // Listen for commands from all user rooms
                val listenCommandsJob = launch {
                    manager.signalingCommands
                        .filter { it.recipientId == user.id }
                        .collect {
                            println("Sending command to client ${signalingCommandsFormat.encodeToString(it.command)}")
                            sendSerialized(it.command)
                        }
                }

                val startJob = manager.onClientConnected(user)

                runCatching {
                    while (true) {
                        val command = receiveDeserialized<SignalingCommand>()
                        println("Received command: ${signalingCommandsFormat.encodeToString(command)}")
                        when (command) {
                            is Reconnect -> manager.onClientConnected(user)
                            is RoomCommand -> manager.onRoomCommand(user, command)
                        }
                        call.respond(HttpStatusCode.OK)
                    }
                }.onFailure { it.printStackTrace() }

                environment.log.info("Client ${user.name} disconnected")
                manager.onClientDisconnected(user)
                startJob.takeIf { it.isActive }?.cancel()
                listenCommandsJob.cancel()
            }
        }
    }
}
