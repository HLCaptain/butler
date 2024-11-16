plugins {
    alias(libs.plugins.illyan.butler.composeMultiplatformLibrary)
    alias(libs.plugins.illyan.butler.koinForComposeMultiplatform)
}

kotlin {
    sourceSets.commonMain.dependencies {
        implementation(projects.composeApp.core.ui.theme)
        implementation(projects.composeApp.domain)
        implementation(projects.composeApp.domain.settings)

        implementation(libs.kotlinx.coroutines)
        implementation(libs.kotlinx.datetime)
    }
}
