plugins {
    alias(libs.plugins.illyan.butler.kotlinMultiplatformLibrary)
    alias(libs.plugins.illyan.butler.koinForKotlinMultiplatform)
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    sourceSets.commonMain.dependencies {
        implementation(projects.composeApp.core.local)
        implementation(projects.composeApp.core.network)
        implementation(projects.composeApp.core.local)
        implementation(projects.composeApp.domain)
        implementation(projects.shared.model)

        implementation(libs.kotlinx.coroutines)
        implementation(libs.kotlinx.serialization.json)
        implementation(libs.napier)

        implementation(libs.multiplatformSettings)
        implementation(libs.multiplatformSettings.coroutines)
        implementation(libs.multiplatformSettings.observable)
        implementation(libs.multiplatformSettings.serialization)
    }
}
