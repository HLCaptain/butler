plugins {
    alias(libs.plugins.illyan.butler.kotlinMultiplatformLibrary)
    alias(libs.plugins.illyan.butler.koinForKotlinMultiplatform)
}

kotlin {
    sourceSets.commonMain.dependencies {
        implementation(projects.composeApp.domain)
        implementation(projects.composeApp.data.chat)
        implementation(projects.composeApp.data.message)
        implementation(projects.composeApp.data.resource)
        implementation(projects.composeApp.domain.auth)

        implementation(libs.kotlinx.coroutines)
        implementation(libs.kotlinx.io)
        implementation(libs.napier)
    }
}
