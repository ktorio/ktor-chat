package io.ktor.chat

import io.ktor.server.cio.*

fun main(args: Array<String>) {
    Runtime.getRuntime().addShutdownHook(Thread {
        println("Shutting down...")
    })
    try {
        EngineMain.main(args)
    } catch (e: Exception) {
        println("Failed to start server: $e")
        e.printStackTrace()
    }
}