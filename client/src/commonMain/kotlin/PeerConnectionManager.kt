package io.ktor.chat.client

import io.ktor.chat.*
import io.ktor.client.webrtc.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

private const val ICE_SEPARATOR = '$'

typealias UpdateTrackFn<T> = suspend (mapping: (Map<String, T>) -> Map<String, T>) -> Unit

/**
 * Manages a WebRTC peer connection with a single remote user.
 * 
 * Each PeerConnectionManager instance handles the complete lifecycle of a 1:1 connection:
 * - SDP offer/answer exchange for media negotiation
 * - ICE candidate collection and exchange for connectivity
 * - Media track management (audio/video)
 * - Connection state monitoring
 * 
 * The class differentiates between the "initiator" (caller) and "receiver" (callee)
 * roles, as they have different responsibilities in the connection establishment process.
 */
class PeerConnectionManager(
    /** WebRTC peer connection that handles the actual media connection */
    private val peerConnection: WebRtcPeerConnection,
    /** Whether this peer initiated the call (affects offer/answer flow) */
    private val isInitiator: Boolean,
    /** Room ID where this call is taking place */
    private val roomId: Long,
    /** The remote user we're connecting with */
    private val interlocutor: User,
    /** The current local user */
    private val currentUser: User,
    /** Client for sending signaling messages */
    private val signalingClient: SignalingClient,
    /** Function to update the remote video track collection */
    private val updateRemoteVideoTracks: UpdateTrackFn<WebRtcMedia.VideoTrack>,
    /** Function to update the remote audio track collection */
    private val updateRemoteAudioTracks: UpdateTrackFn<WebRtcMedia.AudioTrack>,
) {
    private val pendingIceMutex = Mutex()

    /**
     * Buffer to store ICE candidates that arrive before the remote description is set
     * These will be applied once the remote description becomes available
     */
    private val pendingIceCandidates = mutableListOf<WebRtc.IceCandidate>()

    /**
     * Listens for incoming media tracks from the remote peer
     */
    private suspend fun listenForTracks(): Unit = peerConnection.trackEvents.collect { event ->
        println("Track update from ${interlocutor.name}: ${event.track.kind}")
        when (event) {
            is TrackEvent.Add -> {
                when (val track = event.track) {
                    is WebRtcMedia.VideoTrack -> updateRemoteVideoTracks {
                        it.toMutableMap().apply { put(interlocutor.name, track) }
                    }

                    is WebRtcMedia.AudioTrack -> updateRemoteAudioTracks {
                        it.toMutableMap().apply { put(interlocutor.name, track) }
                    }
                }
            }

            is TrackEvent.Remove -> {
                when (val track = event.track) {
                    is WebRtcMedia.VideoTrack -> updateRemoteVideoTracks {
                        it.filter { e -> e.key != interlocutor.name && e.value.id != track.id }
                    }

                    is WebRtcMedia.AudioTrack -> updateRemoteAudioTracks {
                        it.filter { e -> e.key != interlocutor.name && e.value.id != track.id }
                    }
                }
            }
        }
    }

    /**
     * Listens for locally generated ICE candidates and sends them to the remote peer
     * ICE candidates represent possible network paths for connection
     */
    private suspend fun listenForIce(): Unit = peerConnection.iceCandidates.collect {
        println("[ICE] Sending ICE candidate to ${interlocutor.name}: $it")
        // Format the candidate as a string with separators for transmission
        val message = "${it.sdpMLineIndex}$ICE_SEPARATOR${it.sdpMid}$ICE_SEPARATOR${it.candidate}"
        // Create and send the IceExchange command to the specific recipient
        val command = IceExchange(roomId, sender = currentUser, recipientId = interlocutor.id, candidate = message)
        signalingClient.sendCommand(command)
    }

    /**
     * Sets up all the necessary event listeners for the WebRTC connection
     * @param scope CoroutineScope in which to launch the listeners
     */
    fun setupListeners(scope: CoroutineScope) {
        // Monitor connection statistics for debugging
        scope.launch {
            peerConnection.stats.collect { println("Stats for ${interlocutor.name}: $it") }
        }
        scope.launch {
            peerConnection.iceConnectionState.collect { println("ICE connection state for ${interlocutor.name}: $it") }
        }
        scope.launch {
            peerConnection.signalingState.collect { println("Signaling state for ${interlocutor.name}: $it") }
        }
        scope.launch {
            peerConnection.iceGatheringState.collect { println("ICE gathering state for ${interlocutor.name}: $it") }
        }
        scope.launch {
            peerConnection.state.collect {
                println("Connection state for ${interlocutor.name}: $it")
                // If the connection fails, and we're the initiator, restart ICE
                if (it == WebRtc.ConnectionState.FAILED && isInitiator) {
                    peerConnection.restartIce() // will trigger onnegotiationneeded event
                }
            }
        }
        scope.launch {
            // When renegotiation is needed (adding/removing tracks, ICE restart)
            peerConnection.negotiationNeeded.collect {
                sendOffer() // renegotiate
            }
        }
        scope.launch { listenForTracks() }
        scope.launch { listenForIce() }
    }

    /**
     * Sets the remote session description and applies any pending ICE candidates
     */
    private suspend fun setRemoteDescription(description: WebRtc.SessionDescription) {
        println("[SDP] Setting remote description for ${interlocutor.name}: $description")
        pendingIceMutex.withLock {
            // Set the remote description (SDP) received from the other peer
            peerConnection.setRemoteDescription(description)
            // Now that we have the remote description, we can apply any ICE candidates received before the description
            pendingIceCandidates.forEach { peerConnection.addIceCandidate(it) }
            pendingIceCandidates.clear()
        }
    }

    /**
     * Creates and sends an SDP offer to the remote peer
     * Called by the initiator to start the call or during renegotiation
     */
    suspend fun sendOffer() {
        require(isInitiator) { "Only initiator can send offer" }

        val offer = peerConnection.createOffer()
        peerConnection.setLocalDescription(offer)
        println("[SDP] Created offer for ${interlocutor.name}: ${offer.sdp}")

        val command = PickUpCall(roomId, sender = currentUser, recipientId = interlocutor.id, sdpOffer = offer.sdp)
        signalingClient.sendCommand(command)
    }

    /**
     * Processes an incoming SDP offer and generates an appropriate answer
     * Called by the receiver when they get a PickUpCall with an offer
     * 
     * @param sdpOffer The SDP offer string from the initiator
     */
    suspend fun handleOffer(sdpOffer: String) {
        require(!isInitiator) { "Only non-initiator can handle offer" }
        // Create a session description from the offer string
        val offer = WebRtc.SessionDescription(WebRtc.SessionDescriptionType.OFFER, sdpOffer)
        println("[SDP] Set remote description from ${interlocutor.name}: $offer")
        setRemoteDescription(offer)

        // Create an answer that's compatible with the offer
        val answer = peerConnection.createAnswer()
        peerConnection.setLocalDescription(answer)

        // Send the answer back to the initiator
        val command = SdpAnswer(roomId, sender = currentUser, recipientId = interlocutor.id, sdpAnswer = answer.sdp)
        signalingClient.sendCommand(command)
    }

    /**
     * Processes an incoming SDP answer from the remote peer
     */
    suspend fun handleAnswer(sdpAnswer: String) {
        require(isInitiator) { "Only initiator can handle answer" }
        val answer = WebRtc.SessionDescription(WebRtc.SessionDescriptionType.ANSWER, sdp = sdpAnswer)
        setRemoteDescription(answer)
    }

    /**
     * Processes an incoming ICE candidate from the remote peer
     */
    suspend fun handleIce(candidate: String) {
        // Parse the serialized candidate string back into components
        val iceArray = candidate.split(ICE_SEPARATOR)
        val iceCandidate = WebRtc.IceCandidate(
            sdpMLineIndex = iceArray[0].toInt(),
            sdpMid = iceArray[1],
            candidate = iceArray[2]
        )
        println("[ICE] Parsed ICE candidate from ${interlocutor.name}: $iceCandidate")

        pendingIceMutex.withLock {
            // ICE candidates can only be added after the remote description is set
            // If the remote description isn't ready yet, store candidates for later
            if (peerConnection.remoteDescription == null) {
                println("[ICE] Peer connection not ready for ICE from ${interlocutor.name}")
                pendingIceCandidates.add(iceCandidate)
                return
            }
        }

        // If we have a remote description, we can add the candidate immediately
        println("[ICE] Adding ICE candidate from ${interlocutor.name}: $iceCandidate")
        peerConnection.addIceCandidate(iceCandidate)
    }

    /**
     * Closes the peer connection and cleans up resources
     * Should be called when the call ends, or some peer leaves
     */
    suspend fun close() {
        // Close the WebRTC connection, releasing all resources
        peerConnection.close()
        // Clean up any pending candidates to avoid memory leaks
        pendingIceMutex.withLock {
            pendingIceCandidates.clear()
        }
    }
}
