allprojects {
    repositories {
        google()
        mavenCentral()
        mavenLocal()
        maven("https://jitpack.io")
        maven("https://central.sonatype.com/")
        maven("https://s01.oss.sonatype.org/content/repositories/snapshots")
        gradlePluginPortal()
    }
}

plugins {
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.jetbrains.compose) apply false
    alias(libs.plugins.google.ksp) apply false
    alias(libs.plugins.buildconfig) apply false
    alias(libs.plugins.aboutlibraries) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.compose.hot.reload) apply false
    alias(libs.plugins.androidx.room) apply false

    // Convention plugins
    alias(libs.plugins.illyan.butler.kotlinMultiplatform) apply false
    alias(libs.plugins.illyan.butler.kotlinMultiplatformLibrary) apply false
    alias(libs.plugins.illyan.butler.composeMultiplatform) apply false
    alias(libs.plugins.illyan.butler.composeMultiplatformLibrary) apply false
    alias(libs.plugins.illyan.butler.koinForKotlinMultiplatform) apply false
    alias(libs.plugins.illyan.butler.koinForComposeMultiplatform) apply false
}
