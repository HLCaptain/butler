import org.jetbrains.compose.internal.utils.localPropertiesFile

plugins {
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.compose)
    alias(libs.plugins.google.ksp)
    alias(libs.plugins.google.services)
}

repositories {
    mavenCentral()
}

android {
    signingConfigs {
        val properties = localPropertiesFile.readLines().associate {
            if (it.startsWith("#") || !it.contains("=")) return@associate "" to ""
            val (key, value) = it.split("=", limit = 2)
            key to value
        }

        val debugStorePath = properties["DEBUG_KEY_PATH"].toString()
        val debugKeyAlias = properties["DEBUG_KEY_ALIAS"].toString()
        val debugStorePassword = properties["DEBUG_KEYSTORE_PASSWORD"].toString()
        val debugKeyPassword = properties["DEBUG_KEY_PASSWORD"].toString()
        getByName("debug") {
            storeFile = file(debugStorePath)
            keyAlias = debugKeyAlias
            storePassword = debugStorePassword
            keyPassword = debugKeyPassword
        }
        val releaseStorePath = properties["RELEASE_KEY_PATH"].toString()
        val releaseKeyAlias = properties["RELEASE_KEY_ALIAS"].toString()
        val releaseStorePassword = properties["RELEASE_KEYSTORE_PASSWORD"].toString()
        val releaseKeyPassword = properties["RELEASE_KEY_PASSWORD"].toString()
        create("release") {
            storeFile = file(releaseStorePath)
            keyAlias = releaseKeyAlias
            storePassword = releaseStorePassword
            keyPassword = releaseKeyPassword
        }
    }
    namespace = "illyan.butler"
    compileSdk = 34
    defaultConfig {
        applicationId = "illyan.butler"
        minSdk = 26
        targetSdk = 34
        versionCode = 4
        versionName = project.version.toString()
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("release")
        }
    }
    buildFeatures {
        buildConfig = true
    }
    applicationVariants.all {
        val variantName = name
        sourceSets {
            getByName("main") {
                java.srcDir(File("build/generated/ksp/$variantName/kotlin"))
            }
        }
    }
    compileOptions {
        isCoreLibraryDesugaringEnabled = true
    }
    kotlin {
        jvmToolchain(17)
    }
    packaging {
        resources {
            excludes += "/META-INF/**"
        }
    }
}

dependencies {
    implementation(project(":common"))
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.google.firebase.bom))
    implementation(libs.google.firebase.common)

    implementation(platform(libs.koin.bom))
    implementation(libs.koin.core)
    implementation(libs.koin.android)
    implementation(libs.koin.androidx.compose)
    ksp(libs.koin.ksp)

    coreLibraryDesugaring(libs.desugar)
}

tasks.register("BuildAndRun") {
    doFirst {
        exec {
            workingDir(projectDir.parentFile)
            commandLine("./gradlew", "android:build")
            commandLine("./gradlew", "android:installDebug")
        }
    }
}
