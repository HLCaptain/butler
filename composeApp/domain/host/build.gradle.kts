plugins {
    alias(libs.plugins.illyan.butler.kotlinMultiplatformLibrary)
    alias(libs.plugins.illyan.butler.koinForKotlinMultiplatform)
}

kotlin {
    sourceSets.commonMain.dependencies {
        implementation(projects.composeApp.core.network)
        implementation(projects.composeApp.core.network.ktor)

        implementation(projects.composeApp.data.host)
        implementation(projects.composeApp.data.credential)

        implementation(projects.composeApp.domain)

        implementation(libs.kotlinx.coroutines)
        implementation(libs.openai.client)
    }
}
