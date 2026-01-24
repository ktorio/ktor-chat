package io.ktor.chat

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlin.time.Instant

interface Identifiable<ID> {
    val id: ID
}

@Serializable(SimplifiedUserSerializer::class)
sealed interface User: Identifiable<ULong> {
    val name: String
}

fun User(id: ULong, name: String) =
    SimplifiedUser(id, name)

@Serializable
data class FullUser(
    override val name: String,
    val email: String,
    val password: String,
    override val id: ULong = 0u,
): User


@Serializable
data class SimplifiedUser(
    override val id: ULong,
    override val name: String
): User

@Serializable
data class Room(
    val name: String,
    override val id: ULong = 0u,
): Identifiable<ULong>

@Serializable
data class Message(
    val author: User,
    val room: ULong,
    val created: Instant,
    val text: String,
    override val id: ULong = 0u,
    val modified: Instant? = null,
): Identifiable<ULong>

@Serializable
data class Membership(
    val room: Room,
    val user: User,
    override val id: ULong = 0u
): Identifiable<ULong>


class SimplifiedUserSerializer : KSerializer<User> {
    override val descriptor: SerialDescriptor
        get() = SimplifiedUser.serializer().descriptor

    override fun serialize(encoder: Encoder, value: User) {
        val simplifiedUser = SimplifiedUser(value.id, value.name)
        encoder.encodeSerializableValue(SimplifiedUser.serializer(), simplifiedUser)
    }

    override fun deserialize(decoder: Decoder): User =
        decoder.decodeSerializableValue(SimplifiedUser.serializer())
}
