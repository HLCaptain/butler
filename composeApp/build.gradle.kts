import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.compose.internal.utils.localPropertiesFile
import org.jetbrains.kotlin.gradle.targets.js.yarn.YarnLockMismatchReport
import org.jetbrains.kotlin.gradle.targets.js.yarn.YarnRootExtension

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.illyan.butler.composeMultiplatform)
    alias(libs.plugins.illyan.butler.koinForComposeMultiplatform)

}

group = "illyan"
version = libs.versions.butler.get()

kotlin {
    sourceSets.commonMain.dependencies {
        implementation(projects.shared)

        implementation(projects.composeApp.core.ui.resources)
        implementation(projects.composeApp.core.ui.components)
        implementation(projects.composeApp.core.ui.utils)
        implementation(projects.composeApp.core.ui.theme)
        implementation(projects.composeApp.core.local.room)
        implementation(projects.composeApp.core.network.ktor)
        implementation(projects.composeApp.config)

        implementation(projects.composeApp.data.chat)
        implementation(projects.composeApp.data.host)
        implementation(projects.composeApp.data.user)
        implementation(projects.composeApp.data.permission)
        implementation(projects.composeApp.data.resource)
        implementation(projects.composeApp.data.settings)
        implementation(projects.composeApp.data.model)
        implementation(projects.composeApp.data.error)
        implementation(projects.composeApp.data.message)

        implementation(projects.composeApp.domain)
        implementation(projects.composeApp.domain.audio)
        implementation(projects.composeApp.domain.auth)
        implementation(projects.composeApp.domain.chat)
        implementation(projects.composeApp.domain.config)
        implementation(projects.composeApp.domain.error)
        implementation(projects.composeApp.domain.host)
        implementation(projects.composeApp.domain.model)
        implementation(projects.composeApp.domain.permission)
        implementation(projects.composeApp.domain.settings)

        implementation(projects.composeApp.di)
        implementation(projects.composeApp.di.repository)
        implementation(projects.composeApp.feature.theme)
        implementation(projects.composeApp.feature.home)
        implementation(projects.composeApp.feature.auth)
        implementation(projects.composeApp.feature.chat)
        implementation(projects.composeApp.feature.error)
        implementation(projects.composeApp.feature.onboarding)
        implementation(projects.composeApp.feature.permission)
        implementation(projects.composeApp.feature.profile)

        implementation(libs.napier)
    }


    sourceSets.androidMain.dependencies {
        implementation(libs.androidx.core)
        implementation(libs.koin.android)
        implementation(libs.kotlinx.coroutines.android)
        implementation(libs.androidx.activity)
        implementation(libs.androidx.activity.compose)
        implementation(libs.compose.ui.tooling)
        implementation(libs.ffmpeg.kit)
    }

    sourceSets.jvmMain.dependencies {
        implementation(compose.preview)
        implementation(compose.desktop.common)
        implementation(compose.desktop.currentOs)
        implementation(libs.kotlinx.coroutines.swing)
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
        versionCode = 4
        versionName = libs.versions.butler.get()
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

//    applicationVariants.all {
//        val variantName = name
//        sourceSets {
//            getByName("main") {
//                java.srcDir(File("build/generated/ksp/$variantName/kotlin"))
//            }
//        }
//    }

    compileOptions {
        isCoreLibraryDesugaringEnabled = true
    }

    packaging {
        resources {
            excludes += "/META-INF/**"
        }
    }

    dependencies {
        implementation(libs.compose.ui.tooling)
        coreLibraryDesugaring(libs.desugar)
    }
}

compose.desktop.application {
    mainClass = "illyan.butler.MainKt"
    nativeDistributions {
        targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
        packageName = "Butler"
        packageVersion = libs.versions.butler.get().takeWhile { it != '-' }
    }
    buildTypes.release.proguard {
        configurationFiles.from(project.file("compose-desktop.pro"))
//        obfuscate.set(true)
    }
}
