rootProject.name = "ktor-chat"

pluginManagement {
    repositories {
        maven("https://maven.pkg.jetbrains.space/public/p/ktor/eap")
        mavenCentral()
        google()
        gradlePluginPortal()
        maven("https://packages.jetbrains.team/maven/p/amper/amper")
        maven("https://www.jetbrains.com/intellij-repository/releases")
        maven("https://packages.jetbrains.team/maven/p/ij/intellij-dependencies")
    }
}

plugins {
    id("org.jetbrains.amper.settings.plugin").version("0.6.0")
}

include(":core")

include(":db")
include(":client")

include(":server")
include(":server:common")
include(":server:rest")
include(":server:admin")


include(":app")
include(":app:common")
include(":app:desktop")
include(":app:android")