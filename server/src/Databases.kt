package io.ktor.chat

import io.ktor.server.application.*
import io.ktor.server.config.property
import io.ktor.server.plugins.di.*
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import java.nio.file.Paths
import kotlin.io.path.exists

fun Application.databaseModule() {
    val mode = if (Paths.get("module.yaml").exists()) "test" else "main"
    val (url, user, driver, password) = property<DatabaseConfig>("database.$mode")
    log.info("Using database: $mode")

    dependencies {
        provide<Database> {
            Database.connect(url, driver, user, password).also { db ->
                transaction(db) {
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