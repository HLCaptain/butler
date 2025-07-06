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
}

room {
    schemaDirectory("$projectDir/schemas")
}
