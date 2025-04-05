plugins {
    alias(libs.plugins.illyan.butler.kotlinMultiplatformLibrary)
    alias(libs.plugins.illyan.butler.koinForKotlinMultiplatform)
}

kotlin {
    sourceSets.commonMain.dependencies {
        implementation(projects.shared.llm)
        implementation(projects.shared.model)

        implementation(projects.composeApp.core.network)
        implementation(projects.composeApp.core.network.ktor)

        implementation(projects.composeApp.data.chat)
        implementation(projects.composeApp.data.credential)
        implementation(projects.composeApp.data.message)
        implementation(projects.composeApp.data.resource)

        implementation(projects.composeApp.di)
        implementation(projects.composeApp.di.coroutines)

        implementation(projects.composeApp.domain)
        implementation(projects.composeApp.domain.auth)
        implementation(projects.composeApp.data.error)

        implementation(libs.kotlinx.coroutines)
        implementation(libs.kotlinx.io)
        implementation(libs.napier)
        implementation(libs.openai.client)
    }
}
