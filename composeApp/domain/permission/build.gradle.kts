plugins {
    alias(libs.plugins.illyan.butler.kotlinMultiplatformLibrary)
    alias(libs.plugins.illyan.butler.koinForKotlinMultiplatform)
}

kotlin {
    sourceSets.commonMain.dependencies {
        implementation(projects.composeApp.domain)
        implementation(projects.composeApp.data.permission)
        implementation(projects.composeApp.data.permission)

        implementation(libs.kotlinx.coroutines)
        implementation(libs.napier)
    }
}
