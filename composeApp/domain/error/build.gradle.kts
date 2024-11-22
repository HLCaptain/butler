plugins {
    alias(libs.plugins.illyan.butler.kotlinMultiplatformLibrary)
    alias(libs.plugins.illyan.butler.koinForKotlinMultiplatform)
}

kotlin {
    sourceSets.commonMain.dependencies {
        implementation(projects.composeApp.data.error)

        implementation(libs.ktor.core)
        implementation(libs.kotlinx.coroutines)
    }
}
