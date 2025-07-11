import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeHotReload)
}

kotlin {
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        outputModuleName = "composeApp"
        browser {
            val rootDirPath = project.rootDir.path
            val projectDirPath = project.projectDir.path

            commonWebpackConfig {
                outputFileName = "composeApp.js"
                devServer = (devServer ?: KotlinWebpackConfig.DevServer()).apply {
                    port = 3000
                    static = (static ?: mutableListOf()).apply {
                        // Serve sources to debug inside browser
                        //add(rootDirPath)
                        add(projectDirPath)
                        add("$rootDirPath/app/common")
                        add("$rootDirPath/client/src")
                    }
                }
            }
            compilerOptions {
                freeCompilerArgs.add("-Xwasm-attach-js-exception")
                freeCompilerArgs.add("-Xwasm-debugger-custom-formatters")
            }
        }
        binaries.executable()
    }

    sourceSets {
        wasmJsMain {
            dependencies {
                implementation(project(":app:common"))
            }
        }
    }
}