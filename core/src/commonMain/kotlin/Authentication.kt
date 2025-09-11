package io.ktor.chat

import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(val email: String, val password: String)

@Serializable
data class LoginResponse(val token: String, val user: User)

@Serializable
data class RegistrationRequest(val name: String, val email: String, val password: String)

@Serializable
data class RegistrationResponse(val token: String, val user: User, val code: String)

@Serializable
data class ConfirmationRequest(val code: String)