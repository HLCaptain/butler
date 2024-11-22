import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.illyan.butler.composeMultiplatformLibrary)
    alias(libs.plugins.illyan.butler.koinForComposeMultiplatform)
}

kotlin {
    sourceSets.commonMain.dependencies {
        implementation(projects.composeApp.core.ui.components)
        implementation(projects.composeApp.core.ui.resources)
        implementation(projects.composeApp.domain)
        implementation(projects.composeApp.domain.permission)

        implementation(libs.kotlinx.coroutines)
        implementation(libs.kotlinx.datetime)
    }

    sourceSets.androidMain.dependencies {
        implementation(libs.androidx.appcompat)
        implementation(libs.androidx.activity)
    }
}
