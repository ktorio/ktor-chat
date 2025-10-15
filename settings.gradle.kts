rootProject.name = "ktor-chat"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        mavenLocal()
        maven { url = uri("https://maven.pkg.jetbrains.space/public/p/ktor/eap") }
    }

    versionCatalogs {
        create("kotlinWrappers") {
            val wrappersVersion = "2025.7.10"
            from("org.jetbrains.kotlin-wrappers:kotlin-wrappers-catalog:$wrappersVersion")
        }
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

include(":app")
include(":app:common")
include(":app:android")
include(":app:desktop")
include(":app:wasmJs")
include(":server")
include(":server:common")
include(":server:rest")
include(":server:admin")
include(":core")
include(":client")
include(":db")
