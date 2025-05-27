import io.ktor.chat.User
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic

/**
 * Base interface for all WebRTC signaling commands.
 * All commands must specify which room they apply to.
 */
@Serializable
sealed interface SignalingCommand {
    val roomId: Long
}

/**
 * Base interface for commands that are directed to a specific user.
 * Contains sender information and the intended recipient ID.
 */
sealed interface DirectedCommand : SignalingCommand {
    val sender: User
    val recipientId: Long
}

/**
 * Notification sent by the server to indicate there's an ongoing call in a room.
 * 
 * When sent: By the server when a user connects to a room that already has an active call.
 * To whom: Sent to the newly connected user.
 * Purpose: Informs the UI that the user is joining a room with an active call.
 */
@Serializable
class OngoingCall(override val roomId: Long) : SignalingCommand

/**
 * Request to join or initiate a video call in a room.
 * 
 * When sent: 
 * - When a user starts a new call
 * - When a user accepts an incoming call request
 * - After establishing a connection to notify others that user has joined
 * 
 * To whom: Broadcast to all users in the room.
 * 
 * Purpose: Initiates the call setup process or notifies others about a new participant.
 * This is the first step in establishing a WebRTC connection.
 */
@Serializable
class JoinCall(override val roomId: Long, val sender: User) : SignalingCommand

/**
 * Contains the SDP (Session Description Protocol) offer for establishing a WebRTC connection.
 * 
 * When sent: In response to a JoinCall when initiating a connection with another user.
 * 
 * To whom: Sent directly to a specific recipient (specified by recipientId).
 * 
 * Purpose: Provides the initial media parameters (codecs, capabilities) that this user supports.
 * The SDP offer contains information about audio/video tracks, codecs, and other media settings
 * necessary for establishing the peer-to-peer connection.
 */
@Serializable
class PickUpCall(
    override val roomId: Long,
    override val sender: User,
    override val recipientId: Long,
    val sdpOffer: String
) : DirectedCommand

/**
 * Contains the SDP answer in response to an SDP offer.
 * 
 * When sent: After receiving and processing a PickUpCall with an SDP offer.
 * 
 * To whom: Sent directly back to the user who sent the original offer.
 * 
 * Purpose: Completes the SDP negotiation process by responding with compatible settings.
 * Once the offer and answer are exchanged, both peers have agreed on the parameters
 * for the media session and can proceed to establish a connection.
 */
@Serializable
class SdpAnswer(
    override val roomId: Long,
    override val sender: User,
    override val recipientId: Long,
    val sdpAnswer: String
) : DirectedCommand

/**
 * Exchanges ICE (Interactive Connectivity Establishment) candidates between peers.
 * 
 * When sent: Multiple times during a connection establishment as network path options are discovered.
 * 
 * To whom: Sent directly to the specific peer you're trying to connect with.
 * 
 * Purpose: Helps peers discover the best path for direct communication.
 * ICE candidates represent different network paths that might work for the connection:
 * - Local network addresses
 * - Public addresses after NAT traversal
 * - Relay servers (TURN) if direct connection isn't possible
 * 
 * Multiple candidates are typically exchanged until an optimal connection is established.
 */
@Serializable
class IceExchange(
    override val roomId: Long,
    override val sender: User,
    override val recipientId: Long,
    val candidate: String
) : DirectedCommand

/**
 * Notification that a user is leaving the call.
 * 
 * When sent: 
 * - When a user deliberately ends a call
 * - When a user navigates away from the call screen
 * - When a user disconnects unexpectedly (sent by the server)
 * 
 * To whom: Broadcast to all users in the room.
 * 
 * Purpose: Allows other participants to clean up resources for this connection,
 * remove the user's video/audio streams from the UI, and update the participant count.
 */
@Serializable
class LeaveCall(
    override val roomId: Long,
    val sender: User
) : SignalingCommand

/**
 * Custom JSON configuration for serializing and deserializing WebRTC signaling commands.
 */
val signalingCommandsFormat = Json {
    serializersModule = SerializersModule {
        polymorphic(SignalingCommand::class) {
            subclass(OngoingCall::class, OngoingCall.serializer())
            subclass(JoinCall::class, JoinCall.serializer())
            subclass(PickUpCall::class, PickUpCall.serializer())
            subclass(IceExchange::class, IceExchange.serializer())
            subclass(LeaveCall::class, LeaveCall.serializer())
        }
    }
}