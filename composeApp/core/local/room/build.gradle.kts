plugins {
    alias(libs.plugins.illyan.butler.kotlinMultiplatformLibrary)
    alias(libs.plugins.illyan.butler.koinForKotlinMultiplatform)
    alias(libs.plugins.google.ksp)
    alias(libs.plugins.androidx.room)
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    sourceSets.commonMain.dependencies {
        implementation(projects.composeApp.config)
        implementation(projects.composeApp.core.local)
        implementation(projects.composeApp.domain)

        implementation(libs.androidx.room.common)
        implementation(libs.androidx.room.runtime)
        implementation(libs.androidx.sqlite.bundled)
        implementation(libs.kotlinx.serialization.json)
    }
}

dependencies {
    annotationProcessor(libs.androidx.room.compiler)
    add("kspCommonMainMetadata", libs.androidx.room.compiler)
    add("kspAndroid", libs.androidx.room.compiler)
    add("kspJvm", libs.androidx.room.compiler)
}

room {
    schemaDirectory("$projectDir/schemas")
}
