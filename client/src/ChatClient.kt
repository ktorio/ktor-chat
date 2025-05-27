package ktor.chat.client

import JoinCall
import SignalingCommand
import io.ktor.chat.*
import io.ktor.client.webrtc.WebRTCMedia
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

interface ChatClient :
    AuthClient,
    ServerStatusClient,
    ChatRestClient

interface AuthClient {
    suspend fun verify(): Boolean
    suspend fun login(server: String, email: String, password: String): LoginResponse
    suspend fun register(server: String, email: String, name: String, password: String): RegistrationResponse
    suspend fun confirm(code: String)
    suspend fun logout(server: String)
}

interface ServerStatusClient {
    suspend fun isServerAvailable(server: String): Boolean
}

interface ChatRestClient {
    val rooms: Repository<Room, Long>
    val messages: ObservableRepository<Message, Long>
    val users: ReadOnlyRepository<User, Long>
    val memberships: ObservableRepository<Membership, Long>
}

interface SignalingClient {
    val signalingCommandFlow: SharedFlow<SignalingCommand>

    fun connect(scope: CoroutineScope): Job
    suspend fun sendCommand(command: SignalingCommand)
}

interface CallSessionManager {
    var user: User?

    val callRequests: SharedFlow<JoinCall>
    val connectedUsersCount: StateFlow<Int>

    val localAudioTrack: StateFlow<WebRTCMedia.AudioTrack?>
    val localVideoTrack: StateFlow<WebRTCMedia.VideoTrack?>

    val remoteAudioTracks: StateFlow<Map<String, WebRTCMedia.AudioTrack>>
    val remoteVideoTracks: StateFlow<Map<String, WebRTCMedia.VideoTrack>>

    fun setupRoomCall(scope: CoroutineScope)
    suspend fun listenCommandsFlow()

    suspend fun initiateCall(roomId: Long)

    suspend fun acceptCall(request: JoinCall)

    suspend fun rejectCall(request: JoinCall)

    fun enableMicrophone(enabled: Boolean)
    fun enableCamera(enabled: Boolean)

    suspend fun disconnect()
}