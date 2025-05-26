package io.ktor.chat

import kotlinx.datetime.Instant
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.Serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

interface Identifiable<ID> {
    val id: ID
}

@Serializable(SimplifiedUserSerializer::class)
sealed interface User: Identifiable<Long> {
    val name: String
}

fun User(id: Long, name: String) =
    SimplifiedUser(id, name)

@Serializable
data class FullUser(
    override val name: String,
    val email: String,
    val password: String,
    override val id: Long = 0,
): User


@Serializable
data class SimplifiedUser(
    override val id: Long,
    override val name: String
): User

@Serializable
data class Room(
    val name: String,
    override val id: Long = 0,
): Identifiable<Long>

@Serializable
data class Message(
    val author: User,
    val room: Long,
    val created: Instant,
    val text: String,
    override val id: Long = 0,
    val modified: Instant? = null,
): Identifiable<Long>

@Serializable
data class Membership(
    val room: Room,
    val user: User,
    override val id: Long = 0
): Identifiable<Long>


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
