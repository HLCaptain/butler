import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.compose.internal.utils.localPropertiesFile

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.illyan.butler.composeMultiplatform)
    alias(libs.plugins.illyan.butler.koinForComposeMultiplatform)

}

group = "illyan"
version = libs.versions.butler.get()

kotlin {
    sourceSets.commonMain.dependencies {
        implementation(projects.shared.model)

        implementation(projects.composeApp.core.ui.resources)
        implementation(projects.composeApp.core.ui.components)
        implementation(projects.composeApp.core.ui.utils)
        implementation(projects.composeApp.core.ui.theme)
        implementation(projects.composeApp.core.local.room)
        implementation(projects.composeApp.core.local.datastore)
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
        implementation(projects.composeApp.domain.error)
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
        implementation(projects.composeApp.feature.error)
        implementation(projects.composeApp.feature.onboarding)
        implementation(projects.composeApp.feature.permission)
        implementation(projects.composeApp.feature.profile)

        implementation(libs.napier)
        implementation(libs.androidx.datastore.preferences)
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
        implementation(libs.kotlinx.io)
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

    compileOptions {
        isCoreLibraryDesugaringEnabled = true
    }

    packaging {
        resources {
            excludes += "/META-INF/**"
        }
    }

    dependencies {
        debugImplementation(libs.compose.ui.tooling)
        coreLibraryDesugaring(libs.desugar)
    }
}

compose.desktop.application {
    mainClass = "illyan.butler.MainKt"
    nativeDistributions {
        targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
        packageName = "Butler"
        packageVersion = libs.versions.butler.get().takeWhile { it != '-' }
        linux {
            modules("jdk.security.auth")
        }
    }

    buildTypes.release.proguard {
        version = "7.6.0"
//        isEnabled = true
//        optimize = true
//        obfuscate = true

        configurationFiles.from(project.file("compose-desktop.pro"))
    }
}
