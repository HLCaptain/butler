plugins {
    alias(libs.plugins.illyan.butler.kotlinMultiplatformLibrary)
    alias(libs.plugins.illyan.butler.koinForKotlinMultiplatform)
}

kotlin {
    sourceSets.commonMain.dependencies {
        implementation(projects.composeApp.core.utils)
        implementation(projects.composeApp.core.local)
        implementation(projects.composeApp.core.sync)
        implementation(projects.composeApp.core.network)
        implementation(projects.composeApp.core.local.room)
        implementation(projects.composeApp.data.user)
        implementation(projects.composeApp.data.settings)
        implementation(projects.composeApp.domain)
        implementation(projects.shared.model)

        implementation(libs.kotlinx.coroutines)
        implementation(libs.kotlinx.datetime)
        implementation(libs.store)
        implementation(libs.napier)
    }
}
