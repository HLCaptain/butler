plugins {
    alias(libs.plugins.illyan.butler.kotlinMultiplatformLibrary)
    alias(libs.plugins.illyan.butler.koinForKotlinMultiplatform)
}

kotlin {
    sourceSets.commonMain.dependencies {
        implementation(projects.shared.model)
        implementation(projects.shared.llm)

        implementation(projects.composeApp.core.local)
        implementation(projects.composeApp.core.network)
        implementation(projects.composeApp.core.network.ktor)
        implementation(projects.composeApp.core.local.room)

        implementation(projects.composeApp.data.settings)

        implementation(projects.composeApp.domain)

        implementation(libs.kotlinx.coroutines)
        implementation(libs.napier)
        implementation(libs.openai.client)
    }
}
