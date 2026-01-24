plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.kotlinSerialization)
}

dependencies {
    api(project(":core"))
    api(project(":db"))
    api(libs.ktor.server.core)
    api(libs.ktor.server.cio)
    api(libs.ktor.server.auth)
    api(libs.ktor.server.auth.jwt)
    api(libs.ktor.json)
    api(libs.ktor.server.sse)
    api(libs.ktor.server.call.logging)
    api(libs.ktor.server.config.yaml)
    api(libs.ktor.server.content.negotiation)
    api(libs.ktor.server.di)
    api(libs.kotlinx.serialization.json)
    api(libs.kotlinx.coroutines)
    api(libs.logback.classic)
    api(libs.h2)
    api(libs.h2.r2dbc)
    api(libs.exposed)
    api(libs.exposed.r2dbc)
    
    testImplementation(libs.ktor.server.test.host)
    testImplementation(libs.ktor.client.content.negotiation)
    testImplementation(libs.kotlin.test.junit)
}