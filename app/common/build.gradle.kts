import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinSerialization)
}

kotlin {
    jvm()
    androidTarget {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }
    
    sourceSets {
        commonMain {
            dependencies {
                api(project(":client"))
                api(compose.foundation)
                api(compose.material3)
                api(compose.materialIconsExtended)
                api(compose.uiTooling)
                api(libs.androidx.lifecycle.viewmodel)
            }
        }
        
        androidMain {
            dependencies {
                api(compose.preview)
                api(libs.androidx.activity.compose)
                api(libs.androidx.lifecycle.viewmodel.compose)
                api(libs.androidx.lifecycle.viewmodel.savedstate)
                api(libs.androidx.media3.common)
                api(libs.androidx.media3.ui)
                api(libs.androidx.media3.exoplayer)
                api(libs.getstream.webrtc)
            }
        }
        
        jvmMain {
            dependencies {
                api(compose.desktop.currentOs)
                api(libs.androidx.lifecycle.viewmodel.compose)
                api(libs.androidx.lifecycle.viewmodel.savedstate)
                api(libs.androidx.lifecycle.viewmodel.compose.desktop)
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