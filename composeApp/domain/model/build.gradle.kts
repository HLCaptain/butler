plugins {
    alias(libs.plugins.illyan.butler.kotlinMultiplatformLibrary)
    alias(libs.plugins.illyan.butler.koinForKotlinMultiplatform)
}

kotlin {
    sourceSets.commonMain.dependencies {
        implementation(projects.shared.model)

        implementation(projects.composeApp.data.model)
        implementation(projects.composeApp.domain)

        implementation(libs.kotlinx.coroutines)
    }
}
