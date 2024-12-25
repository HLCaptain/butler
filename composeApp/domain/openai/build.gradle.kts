plugins {
    alias(libs.plugins.illyan.butler.kotlinMultiplatformLibrary)
    alias(libs.plugins.illyan.butler.koinForKotlinMultiplatform)
}

kotlin {
    sourceSets.commonMain.dependencies {
        implementation(projects.shared.llm)
        implementation(projects.composeApp.core.utils)
        implementation(projects.composeApp.core.network)
        implementation(projects.composeApp.domain)

        implementation(libs.kotlinx.coroutines)
    }
}
