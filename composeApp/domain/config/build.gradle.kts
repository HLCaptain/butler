plugins {
    alias(libs.plugins.illyan.butler.kotlinMultiplatformLibrary)
    alias(libs.plugins.illyan.butler.koinForKotlinMultiplatform)
}

kotlin {
    sourceSets.commonMain.dependencies {
        implementation(projects.composeApp.data.settings)
        implementation(projects.composeApp.domain.auth)

        implementation(libs.kotlinx.coroutines)
    }
}
