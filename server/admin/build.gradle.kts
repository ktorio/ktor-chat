plugins {
    alias(libs.plugins.kotlinJvm)
    alias(ktorLibs.plugins.ktor)
}

dependencies {
    implementation(project(":core"))
    implementation(project(":db"))
    implementation(project(":server:common"))
    implementation(project(":server:rest"))
    implementation(ktorLibs.server.core)
    implementation(ktorLibs.server.cio)
    implementation(ktorLibs.server.auth)
    implementation(ktorLibs.server.auth.jwt)
    implementation(ktorLibs.serialization.kotlinx.json)
    implementation(ktorLibs.server.sse)
    implementation(ktorLibs.server.callLogging)
    implementation(ktorLibs.server.config.yaml)
    implementation(ktorLibs.server.contentNegotiation)
    implementation(ktorLibs.server.di)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.coroutines)
    implementation(libs.logback.classic)
    implementation(libs.h2)
    implementation(libs.exposed)
    implementation(libs.exposed.r2dbc)
    implementation(libs.postgresql)

    testImplementation(ktorLibs.server.testHost)
    testImplementation(ktorLibs.client.contentNegotiation)
    testImplementation(libs.kotlin.test.junit)
}

application {
    mainClass.set("io.ktor.server.cio.EngineMain")
}
