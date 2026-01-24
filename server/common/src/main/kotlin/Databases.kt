package io.ktor.chat

import io.ktor.server.application.*
import io.ktor.server.config.property
import io.ktor.server.plugins.di.*
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.v1.r2dbc.R2dbcDatabase
import org.jetbrains.exposed.v1.r2dbc.SchemaUtils
import org.jetbrains.exposed.v1.r2dbc.transactions.suspendTransaction
import java.nio.file.Paths
import kotlin.io.path.exists

fun Application.database() {
    val mode = if (Paths.get("build.gradle.kts").exists()) "test" else "main"
    val (url, user, _, password) = property<DatabaseConfig>("database.$mode")
    log.info("Using database: $mode")

    dependencies {
        provide<R2dbcDatabase> {
            R2dbcDatabase.connect(url, user = user, password = password).also { db ->
                suspendTransaction(db) {
                    SchemaUtils.create(Users, Rooms, Messages, Members)
                }
            }
        }
    }
}

@Serializable
data class DatabaseConfig(
    val url: String,
    val user: String,
    val driver: String,
    val password: String
)