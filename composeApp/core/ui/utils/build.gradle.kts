plugins {
    alias(libs.plugins.illyan.butler.composeMultiplatformLibrary)
}

kotlin {
    sourceSets.androidMain.dependencies {
        implementation(libs.androidx.activity.compose)
    }
}
