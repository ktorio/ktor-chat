import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeHotReload)
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    sourceSets {
        androidMain.dependencies {
            implementation(project(":app:common"))
            implementation(libs.androidx.activity.compose)
        }
    }
}

android {
    namespace = "io.ktor.chat.app.android"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    
    defaultConfig {
        applicationId = "io.ktor.chat.app"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
    }
    
    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    
    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.composeMultiplatform.get()
    }

    lint {
        disable += "NullSafeMutableLiveData"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "/commonMain/default/manifest"
            excludes += "/nativeMain/default/manifest"
            excludes += "/commonMain/default/linkdata/module"
            excludes += "/nativeMain/default/linkdata/module"
            excludes += "/META-INF/kotlin-project-structure-metadata.json"
        }
    }
}