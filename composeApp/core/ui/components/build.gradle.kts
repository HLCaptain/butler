plugins {
    alias(libs.plugins.illyan.butler.composeMultiplatformLibrary)
}

kotlin {
    sourceSets.commonMain.dependencies {
        implementation(projects.composeApp.domain)

        implementation(projects.composeApp.core.ui.resources)
        implementation(projects.composeApp.core.ui.utils)
        implementation(projects.composeApp.core.ui.theme)

        implementation(libs.jetbrains.navigation.compose)
    }
}
