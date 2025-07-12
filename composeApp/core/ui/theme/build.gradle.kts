plugins {
    alias(libs.plugins.illyan.butler.composeMultiplatformLibrary)
}

kotlin {
    sourceSets.commonMain.dependencies {
        implementation(projects.composeApp.domain)
        implementation(libs.material.kolors)
    }

    sourceSets.androidMain.dependencies {
        implementation(libs.androidx.appcompat)
        implementation(libs.androidx.activity)
    }
}