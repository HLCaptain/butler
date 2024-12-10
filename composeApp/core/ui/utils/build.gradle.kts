plugins {
    alias(libs.plugins.illyan.butler.composeMultiplatformLibrary)
}

kotlin {
    sourceSets.commonMain.dependencies {
        implementation(libs.material.kolors)
    }

    sourceSets.androidMain.dependencies {
        implementation(libs.androidx.activity.compose)
    }
}
