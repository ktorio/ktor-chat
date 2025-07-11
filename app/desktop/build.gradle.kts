import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeHotReload)
}

dependencies {
    implementation(project(":app:common"))
    implementation(compose.desktop.currentOs)
    implementation(libs.logback.classic)
}

compose.desktop {
    application {
        mainClass = "io.ktor.chat.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "io.ktor.chat"
            packageVersion = "1.0.0"
        }
    }
}