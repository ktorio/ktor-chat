import com.codingfeline.buildkonfig.compiler.FieldSpec.Type.STRING
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.plugin.mpp.NativeBuildType
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.*

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.buildKonfig)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinSerialization)
    kotlin("native.cocoapods")
}

kotlin {
    jvm()
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    iosX64()
    iosArm64()
    iosSimulatorArm64()

    cocoapods {
        version = "1.0"
        summary = "Ktor Chat"
        homepage = "https://github.com/ktorio/ktor-chat/"
        ios.deploymentTarget = "16.0"

        pod("WebRTC-SDK") {
            version = "137.7151.04"
            moduleName = "WebRTC"
            packageName = "WebRTC"
        }

        podfile = project.file("../../iosApp/Podfile")

        framework {
            baseName = "ComposeApp"
            isStatic = true
        }

        xcodeConfigurationToNativeBuildType["CUSTOM_DEBUG"] = NativeBuildType.DEBUG
        xcodeConfigurationToNativeBuildType["CUSTOM_RELEASE"] = NativeBuildType.RELEASE
    }

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
        binaries.executable()
    }

    applyDefaultHierarchyTemplate()

    sourceSets {
        commonMain {
            dependencies {
                api(project(":client"))
                api(compose.foundation)
                api(compose.material3)
                api(compose.materialIconsExtended)
                api(libs.androidx.lifecycle.viewmodel)
                api(libs.androidx.lifecycle.viewmodel.compose)
            }
        }

        androidMain {
            dependencies {
                api(compose.preview)
                api(compose.uiTooling)
                api(libs.androidx.activity.compose)
                api(libs.androidx.lifecycle.viewmodel.savedstate)
                api(libs.getstream.webrtc)
            }
        }

        jvmMain {
            dependencies {
                api(compose.desktop.currentOs)
                api(libs.androidx.lifecycle.viewmodel.savedstate)
                api(libs.androidx.lifecycle.viewmodel.compose.desktop)
            }
        }

        wasmJsMain {
            dependencies {
                api(kotlinWrappers.browser)
                api(libs.androidx.lifecycle.viewmodel.compose.wasm)
            }
        }

        commonTest {
            @OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class)
            dependencies {
                implementation(compose.uiTest)
            }
        }
    }
}

dependencies {
    debugImplementation(compose.uiTooling)
}

android {
    namespace = "io.ktor.chat.app.common"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }

    lint {
        disable += "NullSafeMutableLiveData"
    }
}

val appProps by lazy {
    val propertiesFile = rootProject.file("app.properties")
    Properties().apply { propertiesFile.inputStream().use { secret -> load(secret) } }
}

buildkonfig {
    packageName = "io.ktor.chat.app"
    exposeObjectWithName = "BuildKonfig"

    defaultConfigs {}

    targetConfigs {
        fun createConfig(target: String, prefix: String = target) {
            println("Creating $target target config with prefix $prefix")

            create(target) {
                buildConfigField(STRING, "SERVER_URL", appProps["$prefix.server.url"].toString())

                buildConfigField(STRING, "STUN_URL", appProps["$prefix.stun.url"]?.toString(), nullable = true)
                buildConfigField(
                    STRING,
                    "STUN_USERNAME",
                    appProps["$prefix.stun.username"]?.toString(),
                    nullable = true
                )
                buildConfigField(
                    STRING,
                    "STUN_CREDENTIAL",
                    appProps["$prefix.stun.credential"]?.toString(),
                    nullable = true
                )

                buildConfigField(STRING, "TURN_URL", appProps["$prefix.turn.url"]?.toString(), nullable = true)
                buildConfigField(
                    STRING,
                    "TURN_USERNAME",
                    appProps["$prefix.turn.username"]?.toString(),
                    nullable = true
                )
                buildConfigField(
                    STRING,
                    "TURN_CREDENTIAL",
                    appProps["$prefix.turn.credential"]?.toString(),
                    nullable = true
                )
            }
        }

        // Configurations are specific to the target but still have the same names
        createConfig(target = "android")
        createConfig(target = "jvm", prefix = "desktop")
        createConfig(target = "wasmJs", prefix = "web")
        createConfig(target = "ios")
    }
}