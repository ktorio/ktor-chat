plugins {
    alias(libs.plugins.kotlinJvm)
}

dependencies {
    implementation(project(":core"))
    implementation(libs.exposed)
    implementation(libs.exposed.datetime)
    implementation(libs.exposed.r2dbc)
    implementation(libs.h2)
    implementation(libs.h2.r2dbc)
    
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.kotlin.test.junit)
}