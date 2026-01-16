plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.ktor)
    alias(libs.plugins.kotlinSerialization)
}

ktor {
    openApi {
        enabled = true
        debug = true
        codeInferenceEnabled = true
        onlyCommented = false
    }
}

dependencies {
    implementation(project(":server:common"))
    implementation(libs.ktor.server.status.pages)
    implementation(libs.ktor.server.websockets)
    implementation(libs.ktor.server.cors)
    implementation(libs.ktor.server.swagger)
    implementation(libs.postgresql)

    testImplementation(libs.ktor.server.test.host)
    testImplementation(libs.ktor.client.content.negotiation)
    testImplementation(libs.kotlin.test.junit)
    testImplementation(libs.kotlinx.coroutines.debug)
}

application {
    mainClass.set("io.ktor.server.cio.EngineMain")
}
