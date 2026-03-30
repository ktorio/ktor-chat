package io.ktor.chat

import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule

val customSerializersModule = SerializersModule {
    contextual(ULong::class, object: KSerializer<ULong> {
        override val descriptor: SerialDescriptor =
            PrimitiveSerialDescriptor("ULong", PrimitiveKind.LONG)
        override fun serialize(encoder: Encoder, value: ULong): Unit =
            encoder.encodeLong(value.toLong())
        override fun deserialize(decoder: Decoder): ULong =
            decoder.decodeLong().toULong()
    })
}

fun Application.contentNegotiation() {
    install(ContentNegotiation) {
        json(Json {
            encodeDefaults = false
            ignoreUnknownKeys = true
            isLenient = true
            prettyPrint = true
            serializersModule = customSerializersModule
        })
    }
}