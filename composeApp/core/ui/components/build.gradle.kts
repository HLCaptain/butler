import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.illyan.butler.composeMultiplatformLibrary)
}

kotlin {
    sourceSets.commonMain.dependencies {
        implementation(projects.composeApp.core.ui.resources)
        implementation(projects.composeApp.core.ui.utils)

        implementation(libs.jetbrains.navigation.compose)
    }
}
