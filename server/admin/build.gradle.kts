plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.ktor)
}

dependencies {
    implementation(project(":core"))
    implementation(project(":db"))
    implementation(project(":server:common"))
    implementation(project(":server:rest"))
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.cio)
    implementation(libs.ktor.server.auth)
    implementation(libs.ktor.server.auth.jwt)
    implementation(libs.ktor.json)
    implementation(libs.ktor.server.sse)
    implementation(libs.ktor.server.call.logging)
    implementation(libs.ktor.server.config.yaml)
    implementation(libs.ktor.server.content.negotiation)
    implementation(libs.ktor.server.di)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.datetime)
    implementation(libs.kotlinx.coroutines)
    implementation(libs.logback.classic)
    implementation(libs.h2)
    implementation(libs.exposed)
    implementation(libs.exposed.jdbc)
    implementation(libs.postgresql)

    testImplementation(libs.ktor.server.test.host)
    testImplementation(libs.ktor.client.content.negotiation)
    testImplementation(libs.kotlin.test.junit)
}

application {
    mainClass.set("io.ktor.server.cio.EngineMain")
}
