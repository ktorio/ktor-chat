plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.kotlinSerialization)
    alias(ktorLibs.plugins.ktor)
}

ktor {
    openApi {
        enabled = true
    }
}

dependencies {
    implementation(project(":server:common"))
    implementation(ktorLibs.server.statusPages)
    implementation(ktorLibs.server.websockets)
    implementation(ktorLibs.server.cors)
    implementation(ktorLibs.server.swagger)
    implementation(ktorLibs.server.openapi)
    implementation(libs.postgresql)

    testImplementation(ktorLibs.server.testHost)
    testImplementation(ktorLibs.client.contentNegotiation)
    testImplementation(libs.kotlin.test.junit)
    testImplementation(libs.kotlinx.coroutines.debug)
}

application {
    mainClass.set("io.ktor.server.cio.EngineMain")
}
