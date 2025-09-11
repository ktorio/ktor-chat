plugins {
    alias(libs.plugins.kotlinJvm)
}

dependencies {
    implementation(project(":core"))
    implementation(libs.exposed)
    implementation(libs.exposed.datetime)
    implementation(libs.h2)
    
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.exposed.jdbc)
    testImplementation(libs.kotlin.test.junit)
}