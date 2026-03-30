plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.kotlinSerialization)
}

dependencies {
    api(project(":core"))
    api(project(":db"))
    api(ktorLibs.server.core)
    api(ktorLibs.server.cio)
    api(ktorLibs.server.auth)
    api(ktorLibs.server.auth.jwt)
    api(ktorLibs.server.sse)
    api(ktorLibs.server.callLogging)
    api(ktorLibs.server.config.yaml)
    api(ktorLibs.server.contentNegotiation)
    api(ktorLibs.server.di)
    api(ktorLibs.serialization.kotlinx.json)
    api(libs.kotlinx.coroutines)
    api(libs.logback.classic)
    api(libs.h2)
    api(libs.h2.r2dbc)
    api(libs.exposed)
    api(libs.exposed.r2dbc)

    testImplementation(ktorLibs.server.testHost)
    testImplementation(ktorLibs.client.contentNegotiation)
    testImplementation(libs.kotlin.test.junit)
}
