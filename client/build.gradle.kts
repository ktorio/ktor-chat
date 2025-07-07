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
                api(libs.kotlinx.datetime)
                api(libs.ktor.client.core)
                api(libs.ktor.client.webrtc)
                api(libs.ktor.client.websockets)
                api(libs.ktor.client.content.negotiation)
                api(libs.ktor.json)
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
                api(libs.ktor.client.cio)
            }
        }

        jvmTest {
            dependencies {
                implementation(project(":server:rest"))
                implementation(libs.ktor.server.test.host)
                implementation(libs.ktor.server.core)
                implementation(libs.ktor.server.auth)
                implementation(libs.ktor.server.auth.jwt)
                implementation(libs.ktor.json)
                implementation(libs.ktor.server.sse)
                implementation(libs.ktor.server.config.yaml)
                implementation(libs.ktor.server.content.negotiation)
            }
        }

        wasmJsMain {
            dependencies {
                api(libs.ktor.client.js)
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
        api(libs.ktor.client.cio)
    }
}