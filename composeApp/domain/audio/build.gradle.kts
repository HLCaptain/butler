import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.illyan.butler.kotlinMultiplatformLibrary)
    alias(libs.plugins.illyan.butler.koinForKotlinMultiplatform)
}

kotlin {
    sourceSets.commonMain.dependencies {
        implementation(projects.composeApp.domain.permission)
        implementation(projects.composeApp.data.resource)
        implementation(projects.composeApp.domain)
        implementation(projects.shared)

        implementation(libs.kotlinx.coroutines)
        implementation(libs.korge.core) // Kotlin Multiplatform Audio
        implementation(libs.napier)
        implementation(libs.ktor.core)
        implementation(libs.ffmpeg.kit)
        implementation(libs.kotlinx.datetime)
    }
}
