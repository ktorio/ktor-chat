package io.ktor.chat.client

import io.ktor.chat.*
import io.ktor.client.webrtc.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Implementation of CallSessionManager responsible for managing WebRTC video call sessions.
 * Handles signaling, peer connections, media tracks, and call state management.
 *
 * @param signalingClient Client for sending and receiving WebRTC signaling messages
 * @param rtcClient Client for managing WebRTC connections and media
 */
class CallSessionManagerImpl(
    private val signalingClient: SignalingClient,
    private val rtcClient: WebRTCClient,
) : CallSessionManager {
    /** Current active room ID for the call session */
    var roomId: Long? = null

    /** Current user information */
    override var user: User? = null

    /** Flow for incoming call requests */
    private val callRequestsFlow = MutableSharedFlow<JoinCall>()
    override val callRequests: SharedFlow<JoinCall> = callRequestsFlow.asSharedFlow()

    /** Flow tracking number of connected users in the call */
    private val connectedUsersCountFlow = MutableStateFlow(0)
    override val connectedUsersCount: StateFlow<Int> = connectedUsersCountFlow.asStateFlow()

    /** Flow for local video track */
    private val localVideoTrackFlow = MutableStateFlow<WebRTCMedia.VideoTrack?>(null)
    override val localVideoTrack = localVideoTrackFlow.asStateFlow()

    /** Flow for remote video tracks mapped by username */
    private val remoteVideoTracksFlow = MutableStateFlow<Map<String, WebRTCMedia.VideoTrack>>(mapOf())
    override val remoteVideoTracks = remoteVideoTracksFlow.asStateFlow()

    /** Flow for remote audio tracks mapped by username */
    private val remoteAudioTracksFlow = MutableStateFlow<Map<String, WebRTCMedia.AudioTrack>>(mapOf())
    override val remoteAudioTracks = remoteAudioTracksFlow.asStateFlow()

    /** Mutex to ensure thread-safe initialization of media tracks */
    private val initCallMutex = Mutex()

    /** Flow for local audio track */
    private val localAudioTrackFlow = MutableStateFlow<WebRTCMedia.AudioTrack?>(null)
    override val localAudioTrack = localAudioTrackFlow.asStateFlow()

    /** Maps user ID to peer connection manager for active connections */
    private val peerManagers = mutableMapOf<Long, PeerConnectionManager>()

    /** Set of pending join requests that haven't been accepted or rejected */
    private val pendingJoins = mutableSetOf<JoinCall>()

    /**
     * Initializes local audio and video tracks for the call.
     * Will only initialize tracks if they don't already exist.
     */
    private suspend fun initLocalMedia() = initCallMutex.withLock {
        if (localAudioTrackFlow.value != null && localVideoTrackFlow.value != null) {
            return
        }

        val audioConstraints = WebRTCMedia.AudioTrackConstraints(
            echoCancellation = true,
            noiseSuppression = true
        )
        val videoConstraints = WebRTCMedia.VideoTrackConstraints(
            width = 1280,
            height = 720,
            frameRate = 30,
            facingMode = WebRTCMedia.FacingMode.USER,
        )

        // Create and emit the audio track
        val newAudioTrack = rtcClient.createAudioTrack(audioConstraints)
        localAudioTrackFlow.tryEmit(newAudioTrack)

        // Create and emit the video track
        val newVideoTrack = rtcClient.createVideoTrack(videoConstraints)
        localVideoTrackFlow.tryEmit(newVideoTrack)

        println("Initialized local media")
    }

    /**
     * Listens for incoming signaling commands and handles them based on their type.
     * This is the main communication channel for WebRTC signaling.
     */
    override suspend fun listenCommandsFlow() {
        println("Listening for the commands flow")
        signalingClient.signalingCommandFlow.collect { command ->
            if (command !is RoomCommand) {
                error("Received unexpected command: $command")
            }
            // Filter out commands for other rooms (except for a JoinCall which may start a new call)
            // Though we should not face such a situation yet
            if (command.roomId != roomId && command !is JoinCall) {
                println("Received command for another room: $command")
                return@collect
            }
            when (command) {
                is JoinCall -> handleJoin(command)
                is PickUpCall -> handleResponse(command)
                is SdpAnswer -> peerManagers[command.sender.id]?.handleAnswer(command.sdpAnswer)
                is IceExchange -> peerManagers[command.sender.id]?.handleIce(command.candidate)
                is LeaveCall -> handleLeave(command)
                is OngoingCall -> println("There is an ongoing call")
            }
        }
    }

    /**
     * Sets up event listeners for all active peer connections.
     * This should be called when entering a room, joining a call, and rerun on new connections.
     */
    override fun setupRoomCall(scope: CoroutineScope) {
        peerManagers.forEach { it.value.setupListeners(scope) }
    }

    /**
     * Handles an incoming join request from another user.
     * If we're not in a call, stores the request and notifies the UI.
     * If we're already in a call, automatically establishes a connection.
     */
    private suspend fun handleJoin(command: JoinCall) {
        if (this.roomId == null) {
            pendingJoins.add(command)
            callRequestsFlow.emit(command)
            return
        }
        // If we're already in a call and don't have a connection with this user yet, create one and send an offer
        if (command.roomId == roomId && peerManagers[command.sender.id] == null) {
            makeConnection(withUser = command.sender, isInitiator = true).sendOffer()
        }
    }

    /**
     * Creates a new peer connection with another user or returns null if there existing one.
     * This sets up the WebRTC connection and adds local media tracks.
     *
     * @param withUser The user to connect with
     * @param isInitiator Whether this client is initiating the connection (true) or responding (false)
     * @return A PeerConnectionManager to handle the connection
     */
    private suspend fun makeConnection(withUser: User, isInitiator: Boolean): PeerConnectionManager {
        val existing = peerManagers[withUser.id]
        if (existing != null) {
            return existing
        }
        // Create a new peer connection
        val peerConnection = rtcClient.createPeerConnection()

        // Add local tracks to the connection
        peerConnection.addTrack(localVideoTrack.value!!)
        peerConnection.addTrack(localAudioTrack.value!!)

        // Create a manager for this connection
        val connectionManager = PeerConnectionManager(
            peerConnection,
            roomId = roomId!!,
            currentUser = user!!,
            interlocutor = withUser,
            isInitiator = isInitiator,
            signalingClient = signalingClient,
            updateRemoteVideoTracks = { remoteVideoTracksFlow.update(it) },
            updateRemoteAudioTracks = { remoteAudioTracksFlow.update(it) }
        )

        // Store the manager and update connected users count
        peerManagers[withUser.id] = connectionManager
        connectedUsersCountFlow.emit(peerManagers.size)

        return connectionManager
    }

    private suspend fun handleResponse(command: PickUpCall) {
        makeConnection(withUser = command.sender, isInitiator = false).handleOffer(command.sdpOffer)
    }

    private suspend fun handleLeave(command: LeaveCall) {
        val name = command.sender.name
        println("User $name left the call")
        peerManagers.remove(command.sender.id)?.close()
        connectedUsersCountFlow.emit(peerManagers.size)
        remoteAudioTracksFlow.update { it.filterKeys { k -> k != name } }
        remoteVideoTracksFlow.update { it.filterKeys { k -> k != name } }
    }

    /**
     * Sends a join command to the signaling server to notify others in the room
     * that we want to join the call.
     */
    private suspend fun sendJoin() {
        val command = JoinCall(roomId!!, sender = user!!)
        signalingClient.sendCommand(command)
    }

    /**
     * Initiates a new video call in the specified room.
     * Sets up local media and notifies others in the room.
     */
    override suspend fun initiateCall(roomId: Long) {
        if (roomId == this.roomId) {
            return
        }
        this.roomId = roomId
        println("[CallSessionManager] initiateCall to room: $roomId")
        initLocalMedia()
        sendJoin()
    }

    /**
     * Accepts an incoming call request.
     * Initializes media, creates a connection with the caller, and notifies others.
     */
    override suspend fun acceptCall(request: JoinCall) {
        require(pendingJoins.contains(request)) { "Received duplicate join call" }
        initLocalMedia()
        roomId = request.roomId
        makeConnection(withUser = request.sender, isInitiator = true).sendOffer()
        pendingJoins.clear()
        sendJoin() // let others know
    }

    /**
     * Rejects an incoming call request.
     * Simply removes the request from pending joins.
     */
    override suspend fun rejectCall(request: JoinCall) {
        pendingJoins.remove(request)
    }

    /**
     * Enables or disables the microphone during a call.
     */
    override fun enableMicrophone(enabled: Boolean) {
        localAudioTrack.value?.enable(enabled)
    }

    /**
     * Enables or disables the camera during a call.
     */
    override fun enableCamera(enabled: Boolean) {
        localVideoTrack.value?.enable(enabled)
    }

    /**
     * Disconnects from the current call.
     * Sends a leave notification, closes all connections, and releases resources.
     */
    override suspend fun disconnect() {
        val roomId = roomId ?: return
        this.roomId = null

        // Notify others that we're leaving the call
        runCatching {
            val command = LeaveCall(roomId = roomId, sender = user!!)
            signalingClient.sendCommand(command)
        }.onFailure { it.printStackTrace() }

        // Close all peer connections
        peerManagers.values.forEach { it.close() }
        peerManagers.clear()
        pendingJoins.clear()
        connectedUsersCountFlow.emit(0)

        // Clear remote tracks
        remoteAudioTracksFlow.value.forEach { it.value.close() }
        remoteVideoTracksFlow.value.forEach { it.value.close() }

        remoteVideoTracksFlow.emit(mapOf())
        remoteAudioTracksFlow.emit(mapOf())

        // Close and clear local tracks
        localAudioTrackFlow.value?.close()
        localVideoTrackFlow.value?.close()

        localAudioTrackFlow.value = null
        localVideoTrackFlow.value = null
    }
}
