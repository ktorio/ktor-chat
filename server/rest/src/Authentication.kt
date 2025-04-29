package io.ktor.chat.server

import io.ktor.chat.*
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.plugins.BadRequestException
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import org.koin.core.qualifier.named
import org.koin.ktor.ext.inject
import kotlin.math.absoluteValue

const val CONFIRMATION_MAIL_TEMPLATE = """
    You are the newest member of the most exclusive club in town.
    
    Here is your activation code:
    
    %s
    
    Simply pop that baby into your chat app and you're in.
"""

fun Application.auth() {
    val users by inject<Repository<FullUser, Long>>(named("users"))
    val hashAlgorithm by inject<Algorithm>(named("hash"))
    val mailer by inject<Mailer>()
    val audience = environment.config.property("jwt.audience").getString()
    val issuer = environment.config.property("jwt.issuer").getString()

    fun FullUser.generateCode(): String =
        hashAlgorithm.hash(email)
            .hashCode()
            .absoluteValue
            .toString()
            .padStart(6, '0')
            .substring(0, 6)

    authentication {
        jwt {
            val verifier = JWT.require(hashAlgorithm)
                .withAudience(audience)
                .withIssuer(issuer)
                .build()
            verifier(verifier)
            validate { credential ->
                ChatPrincipal(
                    credential["id"]?.toLongOrNull() ?: return@validate null,
                    credential["name"] ?: return@validate null
                )
            }
        }
    }

    routing {
        route("/auth") {
            fun issueToken(user: User) =
                JWT.create()
                    .withAudience(audience)
                    .withIssuer(issuer)
                    .withClaim("id", user.id.toString())
                    .withClaim("name", user.name)
                    .sign(hashAlgorithm)


            post("login") {
                val credential = call.receive<LoginRequest>()
                val user: FullUser? = users.list { it["email"] = credential.email }.firstOrNull()
                if (user != null && user.password == hashAlgorithm.hash(credential.password)) {
                    call.respond(HttpStatusCode.OK, LoginResponse(
                        token = issueToken(user),
                        user = user
                    ))
                } else {
                    call.respond(HttpStatusCode.Unauthorized, "Invalid credentials")
                }
            }
            post("register") {
                val registration = call.receive<RegistrationRequest>()
                val existingUser: FullUser? = users.list().find { it.name == registration.email }
                if (existingUser != null) {
                    call.respond(HttpStatusCode.Conflict, "User already exists")
                } else {
                    val createdUser = try {
                        users.create(
                            FullUser(
                                registration.name,
                                registration.email,
                                hashAlgorithm.hash(registration.password)
                            )
                        )
                    } catch (e: ConflictingArgumentException) {
                        throw ConflictingArgumentException("User with email ${registration.email} already exists", e)
                    }
                    try {
                        val code = createdUser.generateCode()
                        mailer.sendEmail(
                            recipient = createdUser.email,
                            subject = "Welcome to the chat ${createdUser.name}!",
                            body = String.format(CONFIRMATION_MAIL_TEMPLATE, code).trimIndent(),
                        )
                        val message = RegistrationResponse(
                            token = issueToken(createdUser),
                            user = createdUser,
                            code = code,
                        )
                        call.respond(HttpStatusCode.OK, message)
                    } catch (e: Throwable) {
                        users.delete(createdUser.id)
                        throw e
                    }
                }
            }
            post("logout") {
                // TODO token cache
                call.respond(HttpStatusCode.OK)
            }
            authenticate {
                get("verify") {
                    val userId =
                        call.principal<ChatPrincipal>()?.user?.id ?: throw BadRequestException("Bad token")
                    val user = users.get(userId) ?: throw BadRequestException("No user found for $userId")
                    call.respondText("Welcome back, ${user.name}", status = HttpStatusCode.OK)
                }
                post("confirm") {
                    val confirmation = call.receive<ConfirmationRequest>()
                    val userId = call.principal<ChatPrincipal>()?.user?.id ?: throw IllegalAccessException("Bad token")
                    val user = users.get(userId) ?: throw BadRequestException("No user found for $userId")
                    val expectedCode = user.generateCode()
                    require(expectedCode == confirmation.code) { "Invalid code" }
                    call.respond(HttpStatusCode.NoContent)
                }
            }
        }
    }
}

@Serializable
data class ChatPrincipal(val user: User): Principal {
    constructor(id: Long, name: String): this(SimplifiedUser(id, name))
}