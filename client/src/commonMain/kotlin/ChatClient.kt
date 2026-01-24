package io.ktor.chat.client

import io.ktor.chat.*
import io.ktor.client.webrtc.WebRtcMedia
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
    val rooms: Repository<Room, ULong>
    val messages: ObservableRepository<Message, ULong>
    val users: ReadOnlyRepository<User, ULong>
    val memberships: ObservableRepository<Membership, ULong>
}

interface SignalingClient {
    val signalingCommandFlow: SharedFlow<SignalingCommand>

    fun connect(scope: CoroutineScope, token: String): Job
    suspend fun sendCommand(command: SignalingCommand)
}

interface CallSessionManager {
    var user: User?

    val callRequests: SharedFlow<JoinCall>
    val connectedUsersCount: StateFlow<Int>

    val localAudioTrack: StateFlow<WebRtcMedia.AudioTrack?>
    val localVideoTrack: StateFlow<WebRtcMedia.VideoTrack?>

    val remoteAudioTracks: StateFlow<Map<String, WebRtcMedia.AudioTrack>>
    val remoteVideoTracks: StateFlow<Map<String, WebRtcMedia.VideoTrack>>

    fun setupRoomCall(scope: CoroutineScope)
    suspend fun listenCommandsFlow()

    suspend fun initiateCall(roomId: ULong)

    suspend fun acceptCall(request: JoinCall)

    suspend fun rejectCall(request: JoinCall)

    fun enableMicrophone(enabled: Boolean)
    fun enableCamera(enabled: Boolean)

    suspend fun disconnect()
}