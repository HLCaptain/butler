import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.illyan.butler.kotlinMultiplatformLibrary)
}

kotlin {
    sourceSets.commonMain.dependencies {
        implementation(projects.composeApp.core.local)

        implementation(libs.napier)
        implementation(libs.kotlinx.coroutines)
        implementation(libs.store)
    }
}
