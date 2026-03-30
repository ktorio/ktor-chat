@file:OptIn(ExperimentalWasmDsl::class)

import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
}

kotlin {
    jvm()
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    wasmJs {
        browser {
            compilerOptions {
                freeCompilerArgs.add("-Xwasm-attach-js-exception")
                freeCompilerArgs.add("-Xwasm-debugger-custom-formatters")
            }
        }
    }

    sourceSets {
        commonMain {
            dependencies {
                api(project(":core"))
                api(libs.kotlinx.serialization.json)
                api(libs.kotlinx.coroutines)
                api(ktorLibs.client.core)
                api(ktorLibs.client.webrtc)
                api(ktorLibs.client.websockets)
                api(ktorLibs.client.contentNegotiation)
                api(ktorLibs.serialization.kotlinx.json)
            }
        }

        commonTest {
            dependencies {
                implementation(kotlin("test"))
                implementation(libs.kotlinx.coroutines)
            }
        }

        jvmMain {
            dependencies {
                api(ktorLibs.client.cio)
            }
        }

        jvmTest {
            dependencies {
                implementation(project(":server:rest"))
                implementation(ktorLibs.server.testHost)
                implementation(ktorLibs.server.core)
                implementation(ktorLibs.server.auth)
                implementation(ktorLibs.server.auth.jwt)
                implementation(ktorLibs.serialization.kotlinx.json)
                implementation(ktorLibs.server.sse)
                implementation(ktorLibs.server.config.yaml)
                implementation(ktorLibs.server.contentNegotiation)
            }
        }

        wasmJsMain {
            dependencies {
                api(ktorLibs.client.js)
            }
        }
    }
}

android {
    namespace = "io.ktor.chat.client"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }

    dependencies {
        api(ktorLibs.client.cio)
    }
}
