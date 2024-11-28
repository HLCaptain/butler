plugins {
    alias(libs.plugins.illyan.butler.kotlinMultiplatformLibrary)
}

kotlin {
    sourceSets.commonMain.dependencies {
        implementation(libs.uuid)
    }
}
