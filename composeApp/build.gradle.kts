import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.compose.internal.utils.localPropertiesFile
import org.jetbrains.kotlin.gradle.targets.js.yarn.YarnLockMismatchReport
import org.jetbrains.kotlin.gradle.targets.js.yarn.YarnRootExtension

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.compose)
    alias(libs.plugins.google.ksp)
    alias(libs.plugins.aboutlibraries)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.androidx.room)
}

group = "illyan"
version = libs.versions.butler.get()

kotlin {
    androidTarget()
    jvm()
    jvmToolchain(17)

    sourceSets {
        commonMain {
            dependencies {
                implementation(projects.shared)

                implementation(projects.composeApp.core.ui.resources)
                implementation(projects.composeApp.core.ui.components)
                implementation(projects.composeApp.core.ui.utils)
                implementation(projects.composeApp.core.local)
                implementation(projects.composeApp.core.network)

                implementation(projects.composeApp.data.chat)
                implementation(projects.composeApp.data.host)
                implementation(projects.composeApp.data.user)
                implementation(projects.composeApp.data.permission)
                implementation(projects.composeApp.data.resource)
                implementation(projects.composeApp.data.settings)
                implementation(projects.composeApp.data.model)
                implementation(projects.composeApp.data.error)
                implementation(projects.composeApp.data.message)

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
                implementation(projects.composeApp.feature.theme)

                implementation(compose.runtime)
                implementation(compose.runtimeSaveable)
                implementation(compose.ui)
                implementation(compose.foundation)
                implementation(compose.materialIconsExtended)
                implementation(compose.material3)
                implementation(compose.components.resources)
                implementation(compose.preview)
                implementation(compose.uiTooling)
                implementation(compose.uiUtil)

                implementation(libs.androidx.room.common)
                implementation(libs.androidx.room.runtime)
                implementation(libs.androidx.sqlite.bundled)

                implementation(libs.jetbrains.lifecycle.viewmodel.compose)
                implementation(libs.jetbrains.navigation.compose)

                implementation(libs.ktor.core)
                implementation(libs.ktor.auth)
                implementation(libs.ktor.client.cio)
                implementation(libs.ktor.client.content.negotiation)
                implementation(libs.ktor.client.logging)
                implementation(libs.ktor.serialization.kotlinx.protobuf)
                implementation(libs.ktor.serialization.kotlinx.json)
                implementation(libs.ktor.client.encoding)

                api(project.dependencies.platform(libs.koin.bom))
                api(libs.koin.core)
                implementation(libs.koin.annotations)
                implementation(libs.koin.compose)
                implementation(libs.koin.compose.viewmodel)

                implementation(libs.kotlinx.atomicfu)
                implementation(libs.kotlinx.coroutines)
                implementation(libs.kotlinx.serialization.json)
                implementation(libs.kotlinx.datetime)
                implementation(libs.kotlinx.io)

                implementation(libs.aboutlibraries.core)
                implementation(libs.store)
                implementation(libs.filepicker)
                implementation(libs.coil)
                implementation(libs.coil.compose)
                implementation(libs.haze)

                api(libs.napier)
            }
        }

        commonTest.dependencies {
            implementation(libs.koin.test)
            implementation(libs.kotlinx.coroutines.test)
            implementation(kotlin("test-common"))
            implementation(kotlin("test-annotations-common"))
        }

        jvmTest

        androidMain.dependencies {
            implementation(libs.androidx.core)
            implementation(libs.koin.android)
            implementation(libs.koin.logger.slf4j)
            implementation(libs.kotlinx.coroutines.android)
            implementation(libs.androidx.activity)
            implementation(libs.androidx.activity.compose)
            implementation(libs.ffmpeg.kit)
        }

        jvmMain.dependencies {
            implementation(compose.preview)
            implementation(compose.desktop.common)
            implementation(libs.koin.logger.slf4j)
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutines.swing)
        }
    }
}

dependencies {
    annotationProcessor(libs.androidx.room.compiler)
    add("kspAndroid", libs.androidx.room.compiler)
    add("kspJvm", libs.androidx.room.compiler)
    ksp(libs.koin.ksp)
}

ksp {
    arg("KOIN_CONFIG_CHECK", "true")
    arg("USE_KOIN_COMPOSE_VIEWMODEL", "true")
}

kotlin.sourceSets.all {
    languageSettings.optIn("kotlin.experimental.ExperimentalObjCName")
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
        ksp(libs.koin.ksp)
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

rootProject.plugins.withType(org.jetbrains.kotlin.gradle.targets.js.yarn.YarnPlugin::class.java) {
    rootProject.the<YarnRootExtension>().yarnLockMismatchReport = YarnLockMismatchReport.WARNING
    rootProject.the<YarnRootExtension>().reportNewYarnLock = true
    rootProject.the<YarnRootExtension>().yarnLockAutoReplace = true
}

room {
    schemaDirectory("$projectDir/schemas")
}
