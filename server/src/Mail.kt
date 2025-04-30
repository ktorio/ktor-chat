package io.ktor.chat.server

import io.ktor.server.application.*
import io.ktor.server.plugins.di.*
import org.slf4j.Logger

fun interface Mailer {
    suspend fun sendEmail(recipient: String, subject: String, body: String)
}

fun Application.mail() {
    val log = environment.log

    dependencies {
        provide {
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

fun Logger.logMailer() =
    Mailer { to, subject, body ->
        info("""
        |SENT EMAIL
        |    to: $to
        |    subject: $subject
        |    body:\n${body.prependIndent("    ")}
        |""".trimMargin())
    }