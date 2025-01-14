plugins {
    alias(libs.plugins.illyan.butler.kotlinMultiplatformLibrary)
    alias(libs.plugins.illyan.butler.koinForKotlinMultiplatform)
}

kotlin {
    sourceSets.commonMain.dependencies {
        implementation(projects.composeApp.config)
        implementation(projects.composeApp.di)
        implementation(projects.composeApp.domain.error)

        implementation(libs.kotlinx.coroutines)
    }
}
