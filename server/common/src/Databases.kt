package io.ktor.chat.server

import io.ktor.chat.*
import io.ktor.server.application.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.nio.file.Paths
import org.kodein.di.*
import org.kodein.di.ktor.*
import kotlin.io.path.exists

fun Application.database() {
    val mode = if (Paths.get("module.yaml").exists()) "test" else "main"
    log.info("Using database: $mode")
    val database = Database.connect(
        url = environment.config.property("database.$mode.url").getString(),
        user = environment.config.property("database.$mode.user").getString(),
        driver = environment.config.property("database.$mode.driver").getString(),
        password = environment.config.property("database.$mode.password").getString()
    ).also { db ->
        transaction(db) {
            SchemaUtils.create(Users, Rooms, Messages, Members)
        }
    }
    di {
        bind<Database>() with instance(database)
    }
}