@file:OptIn(ExperimentalWasmDsl::class)

import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.compose.internal.utils.localPropertiesFile
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.illyan.butler.composeMultiplatform)
    alias(libs.plugins.illyan.butler.koinForComposeMultiplatform)
    alias(libs.plugins.aboutlibraries)
}

group = "illyan"
version = libs.versions.butler.name.get()

kotlin {

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach {
        it.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
            // Required when using NativeSQLiteDriver
            linkerOpts.add("-lsqlite3")
        }
    }

    wasmJs {
        outputModuleName.set("composeApp-wasm")
        browser {
            val rootDirPath = project.rootDir.path
            val projectDirPath = project.projectDir.path
            commonWebpackConfig {
                outputFileName = "composeApp-wasm.js"
                devServer = (devServer ?: KotlinWebpackConfig.DevServer()).apply {
                    static = (static ?: mutableListOf()).apply {
                        // Serve sources to debug inside browser
                        add(rootDirPath)
                        add(projectDirPath)
                    }
                }
            }
        }
        binaries.executable()
    }

    js {
        browser {
            commonWebpackConfig {
                outputFileName = "composeApp-js.js"
            }
        }
        binaries.executable()
    }

    sourceSets.commonMain.dependencies {
        implementation(projects.shared.model)

        implementation(projects.composeApp.core.ui.resources)
        implementation(projects.composeApp.core.ui.components)
        implementation(projects.composeApp.core.ui.utils)
        implementation(projects.composeApp.core.ui.theme)
        implementation(projects.composeApp.core.local.sql)
        implementation(projects.composeApp.core.local.keyvalue)
        implementation(projects.composeApp.core.network.ktor)
        implementation(projects.composeApp.config)

        implementation(projects.composeApp.data.chat)
        implementation(projects.composeApp.data.credential)
        implementation(projects.composeApp.data.host)
        implementation(projects.composeApp.data.user)
        implementation(projects.composeApp.data.resource)
        implementation(projects.composeApp.data.settings)
        implementation(projects.composeApp.data.model)
        implementation(projects.composeApp.data.error)
        implementation(projects.composeApp.data.message)

        implementation(projects.composeApp.domain)
        implementation(projects.composeApp.domain.audio)
        implementation(projects.composeApp.domain.auth)
        implementation(projects.composeApp.domain.chat)
        implementation(projects.composeApp.domain.host)
        implementation(projects.composeApp.domain.model)
        implementation(projects.composeApp.domain.settings)

        implementation(projects.composeApp.di)
        implementation(projects.composeApp.di.coroutines)
        implementation(projects.composeApp.di.datasource)
        implementation(projects.composeApp.di.repository)
        implementation(projects.composeApp.feature.theme)
        implementation(projects.composeApp.feature.home)
        implementation(projects.composeApp.feature.auth)
        implementation(projects.composeApp.feature.chat)
        implementation(projects.composeApp.feature.dashboard)
        implementation(projects.composeApp.feature.error)
        implementation(projects.composeApp.feature.onboarding)
        implementation(projects.composeApp.feature.permission)
        implementation(projects.composeApp.feature.profile)

        implementation(libs.aboutlibraries.core)
        implementation(libs.aboutlibraries.compose.m3)
        implementation(libs.material.kolors)

        implementation(libs.napier)

        implementation(libs.multiplatformSettings)
        implementation(libs.multiplatformSettings.coroutines)
    }

    sourceSets.nonWebMain.dependencies {
        implementation(libs.androidx.datastore.preferences.core)
    }

    sourceSets.androidMain {
        dependencies {
            implementation(libs.androidx.core)
            implementation(libs.androidx.core.splashscreen)
            implementation(libs.koin.android)
            implementation(libs.kotlinx.coroutines.android)
            implementation(libs.androidx.activity)
            implementation(libs.androidx.activity.compose)
            implementation(libs.compose.ui.tooling)
        }
    }

    sourceSets.jvmMain {
        dependencies {
            implementation(compose.preview)
            implementation(compose.desktop.common)
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutines.swing)
            implementation(libs.kotlinx.io)
        }
    }
}

val localProperties = localPropertiesFile.readLines().associate {
    if (it.startsWith("#") || !it.contains("=")) return@associate "" to ""
    val (key, value) = it.split("=", limit = 2)
    key to value
}

android {
    namespace = "illyan.butler"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    defaultConfig {
        applicationId = "illyan.butler"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.compileSdk.get().toInt()
        versionCode = libs.versions.butler.code.get().toInt()
        versionName = libs.versions.butler.name.get()
    }

    signingConfigs {
        val debugStorePath = localProperties["DEBUG_KEY_PATH"].toString()
        val debugKeyAlias = localProperties["DEBUG_KEY_ALIAS"].toString()
        val debugStorePassword = localProperties["DEBUG_KEYSTORE_PASSWORD"].toString()
        val debugKeyPassword = localProperties["DEBUG_KEY_PASSWORD"].toString()
        getByName("debug") {
            storeFile = file(debugStorePath)
            keyAlias = debugKeyAlias
            storePassword = debugStorePassword
            keyPassword = debugKeyPassword
        }
        val releaseStorePath = localProperties["RELEASE_KEY_PATH"].toString()
        val releaseKeyAlias = localProperties["RELEASE_KEY_ALIAS"].toString()
        val releaseStorePassword = localProperties["RELEASE_KEYSTORE_PASSWORD"].toString()
        val releaseKeyPassword = localProperties["RELEASE_KEY_PASSWORD"].toString()
        create("release") {
            storeFile = file(releaseStorePath)
            keyAlias = releaseKeyAlias
            storePassword = releaseStorePassword
            keyPassword = releaseKeyPassword
        }
    }

    buildTypes {
        getByName("debug") {
            applicationIdSuffix = ".debug"
        }
        getByName("release") {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("release")
        }
    }

    buildFeatures {
        buildConfig = true
    }

    compileOptions {
        isCoreLibraryDesugaringEnabled = true
    }

    packaging {
        resources {
//            excludes += "/META-INF/**"
        }
    }

    dependencies {
        // libs.compose.ui.tooling not yet compatible with Wasm
//        debugImplementation(libs.compose.ui.tooling)
        coreLibraryDesugaring(libs.desugar)
    }
}

compose.desktop.application {
    mainClass = "illyan.butler.MainKt"
    nativeDistributions {
        targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
        packageName = "Butler"
        packageVersion = libs.versions.butler.name.get().takeWhile { it != '-' }
        linux {
            modules("jdk.security.auth")
        }
    }

    buildTypes.release.proguard {
        version = "7.7.0"
        // FIXME: make JVM prod work with Proguard
        isEnabled = false
//        optimize = true
//        obfuscate = true

        configurationFiles.from(project.file("compose-desktop.pro"))
    }
}

val buildWebApp by tasks.registering(Copy::class) {
    val wasmDist = "wasmJsBrowserDistribution"
    val jsDist = "jsBrowserDistribution"

    dependsOn(wasmDist, jsDist)

    from(tasks.named(jsDist).get().outputs.files)
    from(tasks.named(wasmDist).get().outputs.files)

    into(layout.buildDirectory.dir("webApp"))

    duplicatesStrategy = DuplicatesStrategy.INCLUDE
}

aboutLibraries {
    android {
        registerAndroidTasks = false
    }
    export {
        prettyPrint = true
        outputPath = file("src/commonMain/composeResources/files/aboutlibraries.json")
    }
}
