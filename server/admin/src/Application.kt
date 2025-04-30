package io.ktor.chat

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.exceptions.ExposedSQLException
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction

fun main() {
    Database.connect(
        url = "jdbc:postgresql://host.docker.internal:5440/chat",
        user = "username",
        driver = "org.postgresql.Driver",
        password = "password",
    )

    transaction {
        SchemaUtils.create(Users)
    }

    embeddedServer(CIO, port = 8081) {
        install(Authentication) {
            basic("basic") {
                validate { creds ->
                    if (creds.name == "admin" && creds.password == "admin") {
                        UserIdPrincipal(creds.name)
                    } else {
                        null
                    }
                }
            }
        }

        routing {
            authenticate("basic") {
                delete("/users/{id}") {
                    val log = application.log
                    val id = call.parameters["id"]?.toLongOrNull() ?: return@delete call.respondText("", status = HttpStatusCode.BadRequest)
                    newSuspendedTransaction {
                        try {
                            Users.deleteWhere { Users.id.eq(id) }
                        } catch (cause: ExposedSQLException) {
                            log.error(cause.message)
                        }
                    }

                    call.respond(HttpStatusCode.OK)
                }
            }
        }
    }.start(wait = true)
}