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
        implementation(projects.composeApp.core.utils)
        implementation(projects.composeApp.domain)
        implementation(projects.composeApp.domain.chat)
        implementation(projects.composeApp.domain.auth)
        implementation(projects.composeApp.domain.model)
        implementation(projects.composeApp.domain.audio)
        implementation(projects.composeApp.feature.permission)

        implementation(compose.material3AdaptiveNavigationSuite)
        implementation(libs.material.adaptive)
        implementation(libs.material.adaptive.navigation)
        implementation(libs.material.adaptive.layout)

        implementation(libs.kotlinx.coroutines)
        implementation(libs.kotlinx.datetime)
        implementation(libs.kotlinx.serialization.core)

        implementation(libs.haze)
        implementation(libs.coil.compose)
        implementation(libs.napier)
        implementation(libs.korge.audio)
        implementation(libs.korge.io)
        implementation(libs.filepicker)
        implementation(libs.material.kolors)
    }

    sourceSets.androidMain.dependencies {
        implementation(libs.androidx.appcompat)
        implementation(libs.androidx.activity)
        implementation(libs.androidx.activity.compose)
//        implementation(libs.accompanist.permissions)
        implementation(libs.permissionx)
    }
}
