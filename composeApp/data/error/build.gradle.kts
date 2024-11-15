plugins {
    alias(libs.plugins.illyan.butler.kotlinMultiplatformLibrary)
    alias(libs.plugins.illyan.butler.koinForKotlinMultiplatform)
}

kotlin {
    sourceSets.commonMain.dependencies {
        implementation(projects.composeApp.core.utils)
        implementation(projects.composeApp.domain)
        implementation(projects.shared)

        implementation(libs.kotlinx.coroutines)
        implementation(libs.ktor.core)
        implementation(libs.napier)
    }
}
