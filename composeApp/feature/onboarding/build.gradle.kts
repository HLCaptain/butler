import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.illyan.butler.composeMultiplatformLibrary)
    alias(libs.plugins.illyan.butler.koinForComposeMultiplatform)
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    sourceSets.commonMain.dependencies {
        implementation(projects.composeApp.core.ui.components)
        implementation(projects.composeApp.core.ui.resources)
        implementation(projects.composeApp.core.ui.utils)
        implementation(projects.composeApp.domain)
        implementation(projects.composeApp.domain.settings)
        implementation(projects.composeApp.domain.host)
        implementation(projects.composeApp.feature.chat)
        implementation(projects.composeApp.feature.auth)

        implementation(libs.material.adaptive.navigation.suite)
        implementation(libs.material.adaptive)
        implementation(libs.material.adaptive.navigation)
        implementation(libs.material.adaptive.layout)

        implementation(libs.kotlinx.coroutines)
        implementation(libs.kotlinx.serialization.core)
        implementation(libs.kotlinx.datetime)
        implementation(libs.napier)
    }

    sourceSets.androidMain.dependencies {
        implementation(libs.androidx.appcompat)
        implementation(libs.androidx.activity)
    }
}
