import com.google.devtools.ksp.gradle.KspAATask

plugins {
    alias(libs.plugins.illyan.butler.kotlinMultiplatformLibrary)
    alias(libs.plugins.illyan.butler.koinForKotlinMultiplatform)
    alias(libs.plugins.google.ksp)
    alias(libs.plugins.androidx.room)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.sqlDelight)
}

kotlin {
    sourceSets.commonMain.dependencies {
        implementation(projects.shared.model)

        implementation(projects.composeApp.config)
        implementation(projects.composeApp.core.local)
        implementation(projects.composeApp.domain)
        implementation(projects.composeApp.di)

        implementation(libs.kotlinx.serialization.json)
        implementation(libs.kotlinx.datetime)
        implementation(libs.napier)
    }

    sourceSets.nonWebMain.dependencies {
        implementation(libs.androidx.room.common)
        implementation(libs.androidx.room.runtime)
        implementation(libs.androidx.sqlite.bundled)
    }

    sourceSets.webMain.dependencies {
        implementation(libs.sqldelight.runtime)
        implementation(libs.sqldelight.coroutines.extensions)
        implementation(libs.sqldelight.web.driver)
        implementation(npm("@cashapp/sqldelight-sqljs-worker", libs.versions.sqlDelight.get()))
        implementation(npm("sql.js", libs.versions.sqlJs.get()))
        implementation(devNpm("copy-webpack-plugin", libs.versions.webPackPlugin.get()))
    }
}

sqldelight {
    databases {
        create("ButlerDatabase") {
            generateAsync = true
            packageName.set("illyan.butler.core.local.sqldelight.db")
        }
    }
}

dependencies {
    annotationProcessor(libs.androidx.room.compiler)
    add("kspCommonMainMetadata", libs.androidx.room.compiler)
    add("kspAndroid", libs.androidx.room.compiler)
    add("kspJvm", libs.androidx.room.compiler)
    add("kspIosSimulatorArm64", libs.androidx.room.compiler)
    add("kspIosX64", libs.androidx.room.compiler)
    add("kspIosArm64", libs.androidx.room.compiler)

    // Koin additional KSP config
    add("kspCommonMainMetadata", libs.koin.ksp)
    add("kspAndroid", libs.koin.ksp)
    add("kspJvm", libs.koin.ksp)
    add("kspWasmJs", libs.koin.ksp)
    add("kspJs", libs.koin.ksp)
    add("kspIosX64", libs.koin.ksp)
    add("kspIosArm64", libs.koin.ksp)
    add("kspIosSimulatorArm64", libs.koin.ksp)
}

room {
    schemaDirectory("$projectDir/schemas")
}
