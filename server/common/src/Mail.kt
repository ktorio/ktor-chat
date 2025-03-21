package io.ktor.chat.server

import io.ktor.server.application.*
import org.slf4j.Logger
import org.kodein.di.*
import org.kodein.di.ktor.*

fun interface Mailer {
    suspend fun sendEmail(recipient: String, subject: String, body: String)
}

fun Application.mail() {
    di {
        bind<Mailer>() with singleton {
            Mailer { to, subject, body ->
                log.info(
                    """
                    |SENT EMAIL
                    |    to: $to
                    |    subject: $subject
                    |    body:\n${body.prependIndent("    ")}
                    |""".trimMargin()
                )
            }
        }
    }
}