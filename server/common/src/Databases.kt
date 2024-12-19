package io.ktor.chat

import io.ktor.di.dependencies
import io.ktor.di.invoke
import io.ktor.di.provide
import io.ktor.server.application.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import java.nio.file.Paths
import kotlin.io.path.exists

fun Application.databaseModule() {
    val mode = if (Paths.get("module.yaml").exists()) "test" else "main"
    log.info("Using database: $mode")

    dependencies {
        provide<Database> {
            Database.connect(
                url = property("database.$mode.url"),
                user = property("database.$mode.user"),
                driver = property("database.$mode.driver"),
                password = property("database.$mode.password")
            ).also { db ->
                transaction(db) {
                    SchemaUtils.create(Users, Rooms, Messages, Members)
                }
            }
        }
    }
}