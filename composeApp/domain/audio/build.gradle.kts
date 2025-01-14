plugins {
    alias(libs.plugins.illyan.butler.kotlinMultiplatformLibrary)
    alias(libs.plugins.illyan.butler.koinForKotlinMultiplatform)
}

kotlin {
    sourceSets.commonMain.dependencies {
        implementation(projects.composeApp.data.resource)
        implementation(projects.composeApp.domain)
        implementation(projects.composeApp.di)
        implementation(projects.shared.model)

        implementation(libs.kotlinx.coroutines)
        implementation(libs.korge.audio) // Kotlin Multiplatform Audio
        implementation(libs.korge.io) // Kotlin Multiplatform Audio
        implementation(libs.napier)
        implementation(libs.ktor.core)
        implementation(libs.kotlinx.datetime)
    }
}
