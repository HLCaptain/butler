plugins {
    alias(libs.plugins.illyan.butler.kotlinMultiplatformLibrary)
    alias(libs.plugins.illyan.butler.koinForKotlinMultiplatform)
}

kotlin {
    sourceSets.commonMain.dependencies {
        implementation(projects.shared.model)

        implementation(projects.composeApp.core.local)
        implementation(projects.composeApp.core.network)
        implementation(projects.composeApp.domain)
        implementation(projects.composeApp.config)
        implementation(projects.composeApp.data.credential)
        implementation(projects.composeApp.data.settings)
        implementation(projects.composeApp.data.error)

        implementation(libs.ktor.core)
        implementation(libs.ktor.auth)
        implementation(libs.ktor.client.cio)
        implementation(libs.ktor.client.content.negotiation)
        implementation(libs.ktor.client.logging)
        implementation(libs.ktor.serialization.kotlinx.protobuf)
        implementation(libs.ktor.serialization.kotlinx.json)
        implementation(libs.ktor.client.encoding)

        implementation(libs.kotlinx.datetime)
        implementation(libs.napier)
        implementation(libs.openai.client)
    }
}
