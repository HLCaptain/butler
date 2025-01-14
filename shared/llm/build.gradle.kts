plugins {
    alias(libs.plugins.illyan.butler.kotlinMultiplatformLibrary)
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    sourceSets.commonMain.dependencies {
        implementation(projects.shared.model)

        implementation(libs.kotlinx.serialization.core)
        implementation(libs.kotlinx.datetime)
        implementation(libs.openai.client)

        implementation(libs.napier)
    }
}
