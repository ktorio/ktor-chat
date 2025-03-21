package io.ktor.chat.server

import com.auth0.jwt.algorithms.Algorithm
import io.ktor.server.application.Application
import io.ktor.util.*
import org.kodein.di.*
import org.kodein.di.ktor.di

fun Application.security() {
    di {
        bind<Algorithm>() with singleton {
            Algorithm.HMAC256(
                environment.config.property("security.secret").getString()
            )
        }
    }
}

fun Algorithm.hash(input: String): String =
    sign(input.toByteArray()).encodeBase64()