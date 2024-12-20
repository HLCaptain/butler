plugins {
    alias(libs.plugins.illyan.butler.composeMultiplatformLibrary)
    alias(libs.plugins.illyan.butler.koinForComposeMultiplatform)
}

kotlin {
    sourceSets.commonMain.dependencies {
        implementation(projects.composeApp.core.ui.components)
        implementation(projects.composeApp.core.ui.resources)
        implementation(projects.composeApp.core.ui.theme)
        implementation(projects.composeApp.core.utils)
        implementation(projects.composeApp.feature.theme)
        implementation(projects.composeApp.domain.auth)
        implementation(projects.composeApp.domain)
        implementation(projects.composeApp.domain.settings)
        implementation(projects.composeApp.data.settings)
        implementation(projects.composeApp.data.user)

        implementation(libs.kotlinx.coroutines)
        implementation(libs.kotlinx.datetime)

        implementation(libs.napier)
    }

    sourceSets.androidMain.dependencies {
        implementation(libs.androidx.appcompat)
        implementation(libs.androidx.activity)
        implementation(libs.koin.android)
    }
}
