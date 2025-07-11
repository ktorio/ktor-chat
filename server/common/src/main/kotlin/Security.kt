package io.ktor.chat

import com.auth0.jwt.algorithms.Algorithm
import io.ktor.server.application.Application
import io.ktor.server.config.property
import io.ktor.server.plugins.di.*
import io.ktor.util.*

fun Application.security() {
    val secret: String = property("security.secret")

    dependencies {
        provide<Algorithm>("hash") {
            Algorithm.HMAC256(secret)
        }
    }
}

fun Algorithm.hash(input: String): String =
    sign(input.toByteArray()).encodeBase64()