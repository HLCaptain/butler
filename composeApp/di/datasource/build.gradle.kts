plugins {
    alias(libs.plugins.illyan.butler.kotlinMultiplatformLibrary)
    alias(libs.plugins.illyan.butler.koinForKotlinMultiplatform)
}

kotlin {
    sourceSets.commonMain.dependencies {
        implementation(projects.shared.model)

        implementation(projects.composeApp.config)

        implementation(projects.composeApp.core.network.ktor)
        implementation(projects.composeApp.core.network)

        implementation(libs.kotlinx.coroutines)
    }
}
