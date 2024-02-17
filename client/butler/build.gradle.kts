import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.compose.internal.utils.localPropertiesFile
import org.jetbrains.kotlin.gradle.dsl.KotlinCompile

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.compose)
    alias(libs.plugins.sqldelight)
    alias(libs.plugins.google.ksp)
    alias(libs.plugins.buildconfig)
    alias(libs.plugins.aboutlibraries)
    alias(libs.plugins.google.services)
}

group = "illyan"
version = libs.versions.butler.get()

kotlin {
    js {
        moduleName = rootProject.name
        browser {
            commonWebpackConfig {
                outputFileName = "web.js"
            }
        }
        binaries.executable()
    }

    androidTarget {
        compilations.all {
            kotlinOptions {
                jvmTarget = "1.8"
            }
        }
    }

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
                api(libs.voyager.navigator)
                implementation(libs.voyager.screenModel)
                implementation(libs.voyager.bottomSheetNavigator)
                implementation(libs.voyager.tabNavigator)
                implementation(libs.voyager.transitions)
                implementation(libs.voyager.koin)
                implementation(libs.ktor.core)
                api(project.dependencies.platform(libs.koin.bom))
                api(libs.koin.core)
                implementation(libs.koin.annotations)
                implementation(libs.koin.compose)
                api(libs.napier)
                implementation(libs.store)
                implementation(libs.kotlinx.atomicfu)
                implementation(libs.kotlinx.coroutines)
                implementation(libs.kotlinx.serialization.json)
                implementation(libs.kotlinx.datetime)
                implementation(libs.sqldelight.coroutines)
                implementation(libs.sqldelight.adapters)
                api(libs.gitlive.firebase.common)
                api(libs.gitlive.firebase.auth)
                api(libs.gitlive.firebase.firestore)
                implementation(libs.uuid)
                implementation(libs.aboutlibraries.core)
            }
        }

        commonTest.dependencies {
            implementation(libs.koin.test)
            implementation(libs.kotlinx.coroutines.test)
            implementation(kotlin("test-common"))
            implementation(kotlin("test-annotations-common"))
        }

        jsTest
        jvmTest

        androidMain.dependencies {
            implementation(libs.androidx.appcompat)
            implementation(libs.androidx.core)
            implementation(libs.ktor.jvm)
            implementation(libs.koin.android)
            implementation(libs.koin.logger.slf4j)
            implementation(libs.sqldelight.android)
            implementation(libs.kotlinx.coroutines.android)
            implementation(libs.compose.ui.tooling.preview)
            implementation(libs.androidx.activity.compose)
        }

        jvmMain.dependencies {
            implementation(compose.preview)
            implementation(compose.desktop.common)
            implementation(libs.ktor.jvm)
            implementation(libs.koin.ktor)
            implementation(libs.koin.logger.slf4j)
            implementation(libs.sqldelight.jvm)
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutines.swing)
        }

        jsMain.dependencies {
            implementation(compose.html.core)
            implementation(libs.kotlinx.coroutines.js)
            implementation(libs.sqldelight.js)
            implementation(npm("kotlinx-coroutines-core", "1.7.3"))
            implementation(npm("sql.js", "1.10.2"))
            implementation(npm("dateformat", "5.0.3"))
            implementation(npm("@cashapp/sqldelight-sqljs-worker", "2.0.1"))
            implementation(devNpm("copy-webpack-plugin", "12.0.2"))
        }
    }
}

dependencies {
    add("kspCommonMainMetadata", libs.koin.ksp)
    api(project.dependencies.platform(libs.google.firebase.bom))
    api(libs.google.firebase.common)
    api(libs.google.firebase.auth)
    api(libs.google.firebase.firestore)
}

// WORKAROUND: ADD this dependsOn("kspCommonMainKotlinMetadata") instead of above dependencies
tasks.withType<KotlinCompile<*>>().configureEach {
    if (name != "kspCommonMainKotlinMetadata") {
        dependsOn("kspCommonMainKotlinMetadata")
    }
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
    packageName = "illyan.butler.config"

    // Use local.properties file to store secrets
    val properties = localPropertiesFile.readLines().associate {
        if (it.startsWith("#") || !it.contains("=")) return@associate "" to ""
        val (key, value) = it.split("=", limit = 2)
        key to value
    }

    val firebaseWebAndDesktopApiKey = properties["FIREBASE_WEB_AND_DESKTOP_API_KEY"].toString()
    val firebaseMessagingSenderId = properties["FIREBASE_MESSAGING_SENDER_ID"].toString()
    val firebaseDesktopAppId = properties["FIREBASE_DESKTOP_APP_ID"].toString()
    val firebaseWebAppId = properties["FIREBASE_WEB_APP_ID"].toString()
    val firebaseStorageBucket = properties["FIREBASE_STORAGE_BUCKET"].toString()
    val firebaseProjectId = properties["FIREBASE_PROJECT_ID"].toString()
    val firebaseAuthDomain = properties["FIREBASE_AUTH_DOMAIN"].toString()

    buildConfigField("String", "FIREBASE_WEB_AND_DESKTOP_API_KEY", "\"$firebaseWebAndDesktopApiKey\"")
    buildConfigField("String", "FIREBASE_MESSAGING_SENDER_ID", "\"$firebaseMessagingSenderId\"")
    buildConfigField("String", "FIREBASE_DESKTOP_APP_ID", "\"$firebaseDesktopAppId\"")
    buildConfigField("String", "FIREBASE_WEB_APP_ID", "\"$firebaseWebAppId\"")
    buildConfigField("String", "FIREBASE_STORAGE_BUCKET", "\"$firebaseStorageBucket\"")
    buildConfigField("String", "FIREBASE_PROJECT_ID", "\"$firebaseProjectId\"")
    buildConfigField("String", "FIREBASE_AUTH_DOMAIN", "\"$firebaseAuthDomain\"")
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

compose.experimental {
    web.application {}
}

compose.desktop.application {
    mainClass = "illyan.butler.MainKt"
    nativeDistributions {
        targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
        packageName = "desktop"
        packageVersion = libs.versions.butler.get().takeWhile { it != '-' }
    }
}
