import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.compose.internal.utils.localPropertiesFile
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.targets.js.yarn.YarnLockMismatchReport
import org.jetbrains.kotlin.gradle.targets.js.yarn.YarnRootExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.compose)
    alias(libs.plugins.sqldelight)
    alias(libs.plugins.google.ksp)
    alias(libs.plugins.buildconfig)
    alias(libs.plugins.aboutlibraries)
}

group = "illyan"
version = libs.versions.butler.get()

kotlin {
//    js {
//        moduleName = rootProject.name
//        browser {
//            commonWebpackConfig {
//                outputFileName = "web.js"
//            }
//        }
//        binaries.executable()
//    }

    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    compilerOptions {
//        apiVersion = KotlinVersion.KOTLIN_2_0
//        languageVersion = KotlinVersion.KOTLIN_2_0
    }

//    androidTarget {
//        compilations.all {
//            kotlinOptions {
//                jvmTarget = "1.8"
//            }
//        }
//    }

    androidTarget()

    jvm()

    sourceSets {
        commonMain {
            kotlin.srcDir("build/generated/ksp/metadata/commonMain/kotlin")
            dependencies {
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

                implementation(libs.voyager.navigator)
                implementation(libs.voyager.screenModel)
                implementation(libs.voyager.bottomSheetNavigator)
                implementation(libs.voyager.tabNavigator)
                implementation(libs.voyager.transitions)
                implementation(libs.voyager.koin)

                implementation(libs.ktor.core)
                implementation(libs.ktor.auth)
                implementation(libs.ktor.client.content.negotiation)
                implementation(libs.ktor.client.websockets)
                implementation(libs.ktor.client.logging)
                implementation(libs.ktor.serialization.kotlinx.protobuf)
                implementation(libs.ktor.serialization.kotlinx.json)
                implementation(libs.ktor.client.encoding)

                api(project.dependencies.platform(libs.koin.bom))
                api(libs.koin.core)
                implementation(libs.koin.annotations)
                implementation(libs.koin.compose)

                implementation(libs.kotlinx.atomicfu)
                implementation(libs.kotlinx.coroutines)
                implementation(libs.kotlinx.serialization.json)
                implementation(libs.kotlinx.datetime)

                implementation(libs.sqldelight.coroutines)
                implementation(libs.sqldelight.adapters)

                implementation(libs.uuid)
                implementation(libs.aboutlibraries.core)
                implementation(libs.store)
                implementation(libs.settings)
                implementation(libs.settings.coroutines)

                api(libs.napier)
            }
        }

        commonTest.dependencies {
            implementation(libs.koin.test)
            implementation(libs.kotlinx.coroutines.test)
            implementation(kotlin("test-common"))
            implementation(kotlin("test-annotations-common"))
        }

//        jsTest
        jvmTest

        androidMain.dependencies {
            implementation(libs.androidx.core)
            implementation(libs.androidx.crypto)
            implementation(libs.koin.android)
            implementation(libs.ktor.client.cio)
            implementation(libs.koin.logger.slf4j)
            implementation(libs.sqldelight.android)
            implementation(libs.kotlinx.coroutines.android)
            implementation(libs.androidx.activity)
            implementation(libs.androidx.activity.compose)
            implementation(libs.settings.datastore)
            implementation(libs.androidx.datastore.core)
            implementation(libs.androidx.datastore.preferences)
        }

        jvmMain.dependencies {
            implementation(compose.preview)
            implementation(compose.desktop.common)
            implementation(libs.androidx.crypto)
            implementation(libs.koin.ktor)
            implementation(libs.ktor.client.cio)
            implementation(libs.koin.logger.slf4j)
            implementation(libs.sqldelight.jvm)
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutines.swing)
            implementation(libs.credential.storage.jvm)
            implementation(libs.settings.datastore)
            implementation(libs.androidx.datastore.core)
            implementation(libs.androidx.datastore.preferences)
        }

//        jsMain.dependencies {
//            implementation(libs.ktor.client.js)
//            implementation(compose.material)
//            implementation(compose.html.core)
//            implementation(libs.kotlinx.coroutines.js)
//            implementation(libs.sqldelight.js)
//            implementation(npm("kotlinx-coroutines-core", "1.7.3"))
//            implementation(npm("sql.js", "1.10.2"))
//            implementation(npm("dateformat", "5.0.3"))
//            implementation(npm("@cashapp/sqldelight-sqljs-worker", "2.0.1"))
//            implementation(devNpm("copy-webpack-plugin", "12.0.2"))
//            implementation(devNpm("localstorage-slim", "2.7.0"))
//        }
    }
}

tasks.withType<KotlinJvmCompile>().configureEach {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_1_8
//        languageVersion = KotlinVersion.KOTLIN_2_0
    }
}
tasks.withType<KotlinCompile>().configureEach {
    dependsOn("kspCommonMainKotlinMetadata")
}

dependencies {
    add("kspCommonMainMetadata", libs.koin.ksp)
}

afterEvaluate {
    tasks.filter {
        it.name.contains("SourcesJar", true)
    }.forEach {
        println("SourceJarTask====>${it.name}")
        it.dependsOn("kspCommonMainKotlinMetadata")
    }
}

ksp {
    arg("KOIN_CONFIG_CHECK", "true")
}

sqldelight {
    databases {
        create("Database") {
            packageName = "illyan.butler.db"
            generateAsync = true
        }
    }
}

kotlin.sourceSets.all {
    languageSettings.optIn("kotlin.experimental.ExperimentalObjCName")
}

buildConfig {
    // Setting required for collision avoidance with Android platform BuildConfig
    packageName = "illyan.butler.config"

    // Checking if the task is a debug or release task to set DEBUG flag
    // Parsing main task name to check if it contains debug or release type names
    // Not 100% accurate but should work for most cases
    // Defaults to DEBUG = true
    gradle.taskGraph.whenReady {
        val taskName = allTasks.last().name
        val debugIndicatorNames = listOf("dev", "debug")
        val prodIndicatorNames = listOf("release", "prod")
        val isProd = debugIndicatorNames.none { taskName.contains(it, ignoreCase = true) } &&
            prodIndicatorNames.any { taskName.contains(it, ignoreCase = true) }

        println("Task [$taskName] isProd=$isProd")

        val useMemoryDb = true // Set to false to use SQLDelight database and Ktor, else memory based DB will be used without networking
        buildConfigField("Boolean", "DEBUG", (!isProd).toString())
        buildConfigField("Boolean", "USE_MEMORY_DB", if (isProd) "false" else useMemoryDb.toString()) //
    }

    // GOOGLE_CLIENT_ID from local.properties
    val properties = localPropertiesFile.readLines().associate {
        if (it.startsWith("#") || !it.contains("=")) return@associate "" to ""
        val (key, value) = it.split("=", limit = 2)
        key to value
    }
    val googleClientId = if (properties["GOOGLE_CLIENT_ID"] == null) null else "\"${properties["GOOGLE_CLIENT_ID"]}\""
    buildConfigField("String?", "GOOGLE_CLIENT_ID", "$googleClientId")
    val apiGatewayUrl = if (properties["API_GATEWAY_URL"] == null) null else "\"${properties["API_GATEWAY_URL"]}\""
    buildConfigField("String?", "API_GATEWAY_URL", "$apiGatewayUrl")
}

android {
    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    sourceSets["main"].res.srcDirs("src/androidMain/res")
    sourceSets["main"].resources.srcDirs("src/commonMain/resources")

    namespace = "illyan.butler"
    compileSdk = 34
    defaultConfig {
        applicationId = "illyan.butler"
        minSdk = 26
        targetSdk = 34
        versionCode = 4
        versionName = libs.versions.butler.get()
    }

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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
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

//compose.experimental {
//    web.application {}
//}

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
