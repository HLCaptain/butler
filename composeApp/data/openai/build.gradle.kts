plugins {
    alias(libs.plugins.illyan.butler.kotlinMultiplatformLibrary)
    alias(libs.plugins.illyan.butler.koinForKotlinMultiplatform)
}

kotlin {
    sourceSets.commonMain.dependencies {
        implementation(projects.composeApp.core.utils)
        implementation(projects.composeApp.core.local)
        implementation(projects.composeApp.core.network.ktor)
        implementation(projects.composeApp.domain)

        implementation(libs.kotlinx.coroutines)
        implementation(libs.napier)
    }
}
