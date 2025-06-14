plugins {
    alias(libs.plugins.illyan.butler.kotlinMultiplatformLibrary)
}

kotlin {
    sourceSets.commonMain.dependencies {
        implementation(projects.shared.model)

        implementation(projects.composeApp.domain)

        implementation(libs.kotlinx.coroutines)
    }
}
