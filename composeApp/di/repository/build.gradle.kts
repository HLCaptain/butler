plugins {
    alias(libs.plugins.illyan.butler.kotlinMultiplatformLibrary)
    alias(libs.plugins.illyan.butler.koinForKotlinMultiplatform)
}

kotlin {
    sourceSets.commonMain.dependencies {
        implementation(projects.composeApp.config)
        implementation(projects.composeApp.data.credential)
        implementation(projects.composeApp.data.error)
        implementation(projects.composeApp.data.host)
        implementation(projects.composeApp.data.user)
        implementation(projects.composeApp.data.chat)
        implementation(projects.composeApp.data.model)
        implementation(projects.composeApp.data.message)
        implementation(projects.composeApp.data.resource)
        implementation(projects.composeApp.data.settings)
        implementation(projects.composeApp.domain.error)

        implementation(projects.composeApp.core.network.ktor)
        implementation(projects.composeApp.core.local.room)
    }
}
