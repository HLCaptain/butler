plugins {
    alias(libs.plugins.illyan.butler.kotlinMultiplatformLibrary)
}

kotlin {
    sourceSets.wasmJsMain.dependencies {
        implementation(libs.kotlinx.browser)
    }
}
