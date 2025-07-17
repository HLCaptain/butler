plugins {
    alias(libs.plugins.illyan.butler.kotlinMultiplatformLibrary)
    alias(libs.plugins.illyan.butler.koinForKotlinMultiplatform)
}

kotlin {
    sourceSets.commonMain.dependencies {
        implementation(libs.multiplatformSettings)
        implementation(libs.multiplatformSettings.coroutines)
        implementation(libs.multiplatformSettings.observable)
        implementation(libs.multiplatformSettings.serialization)
    }

    sourceSets.nonWebMain.dependencies {
        implementation(libs.androidx.datastore.preferences.core)
        implementation(libs.multiplatformSettings.datastore)
    }

//    sourceSets.iosMain {
//        dependsOn(sourceSets.nonWebMain.get())
//    }
}
