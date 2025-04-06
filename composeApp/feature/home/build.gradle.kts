plugins {
    alias(libs.plugins.illyan.butler.composeMultiplatformLibrary)
    alias(libs.plugins.illyan.butler.koinForComposeMultiplatform)
    alias(libs.plugins.kotlin.serialization)
}

kotlin {

    sourceSets.commonMain.dependencies {
        implementation(projects.composeApp.config)
        implementation(projects.composeApp.core.ui.components)
        implementation(projects.composeApp.core.ui.resources)
        implementation(projects.composeApp.core.utils)
        implementation(projects.composeApp.core.ui.utils)
        implementation(projects.composeApp.data.credential)
        implementation(projects.composeApp.domain)
        implementation(projects.composeApp.data.error)
        implementation(projects.composeApp.domain.auth)
        implementation(projects.composeApp.domain.chat)
        implementation(projects.composeApp.feature.auth)
        implementation(projects.composeApp.feature.chat)
        implementation(projects.composeApp.feature.onboarding)
        implementation(projects.composeApp.feature.profile)
        implementation(projects.composeApp.feature.error)
        implementation(projects.composeApp.feature.permission)

        implementation(libs.material.adaptive.navigation.suite)
        implementation(libs.material.adaptive)
        implementation(libs.material.adaptive.navigation)
        implementation(libs.material.adaptive.layout)

        implementation(libs.kotlinx.coroutines)
        implementation(libs.kotlinx.datetime)
        implementation(libs.kotlinx.serialization.core)

        implementation(libs.aboutlibraries.core)
        implementation(libs.aboutlibraries.compose.m3)

        implementation(libs.napier)
    }

    sourceSets.androidMain.dependencies {
        implementation(libs.androidx.appcompat)
        implementation(libs.androidx.activity)
    }
}
