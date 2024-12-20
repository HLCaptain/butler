allprojects {
    repositories {
        google()
        mavenCentral()
        mavenLocal()
        maven("https://maven.pkg.jetbrains.space/kotlin/p/kotlin/kotlin-js-wrappers")
        maven("https://jitpack.io")
        maven("https://maven.pkg.jetbrains.space/public/p/kotlinx-coroutines/maven")
        maven("https://oss.sonatype.org/content/repositories/snapshots/")
        maven("https://maven.pkg.jetbrains.space/kotlin/p/kotlin/bootstrap")
        maven("https://androidx.dev/storage/compose-compiler/repository")
        maven("https://maven.pkg.jetbrains.space/kotlin/p/wasm/experimental")
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
        maven("https://maven.pkg.jetbrains.space/kotlin/p/kotlin/dev/")
        maven("https://central.sonatype.com/")
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
    alias(libs.plugins.androidx.room) apply false

    // Convention plugins
    alias(libs.plugins.illyan.butler.kotlinMultiplatform) apply false
    alias(libs.plugins.illyan.butler.kotlinMultiplatformLibrary) apply false
    alias(libs.plugins.illyan.butler.composeMultiplatform) apply false
    alias(libs.plugins.illyan.butler.composeMultiplatformLibrary) apply false
    alias(libs.plugins.illyan.butler.koinForKotlinMultiplatform) apply false
    alias(libs.plugins.illyan.butler.koinForComposeMultiplatform) apply false
}
