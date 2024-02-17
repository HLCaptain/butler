import org.jetbrains.compose.internal.utils.localPropertiesFile
import org.jetbrains.kotlin.gradle.dsl.KotlinCompile

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.android.library)
    alias(libs.plugins.jetbrains.compose)
    alias(libs.plugins.sqldelight)
//    alias(libs.plugins.libres)
    alias(libs.plugins.google.ksp)
    alias(libs.plugins.buildconfig)
}

group = "illyan"
version = libs.versions.butler.get()

kotlin {
    jvmToolchain(17)
    androidTarget()
    jvm()
    js(IR) {
        useCommonJs()
        browser()
    }

    sourceSets {
        commonMain {

        }
        commonMain {
            kotlin.srcDir("build/generated/ksp/metadata/commonMain/kotlin")
            dependencies {
                implementation(compose.runtime)
                implementation(compose.ui)
                implementation(compose.foundation)
                implementation(compose.materialIconsExtended)
                implementation(compose.material3)
                implementation(compose.components.resources)
                implementation(libs.voyager.navigator)
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
            }
        }

        commonTest.dependencies {
            implementation(libs.koin.test)
            implementation(libs.kotlinx.coroutines.test)
            implementation(kotlin("test-common"))
            implementation(kotlin("test-annotations-common"))
        }

        androidMain.dependencies {
            implementation(libs.androidx.appcompat)
            implementation(libs.androidx.core)
            implementation(libs.ktor.jvm)
            implementation(libs.koin.android)
            implementation(libs.koin.logger.slf4j)
            implementation(libs.sqldelight.android)
            implementation(libs.kotlinx.coroutines.android)
        }

        jvmMain.dependencies {
            implementation(compose.preview)
            implementation(compose.desktop.common)
            implementation(libs.ktor.jvm)
            implementation(libs.koin.ktor)
            implementation(libs.koin.logger.slf4j)
            implementation(libs.sqldelight.jvm)
        }

        jsMain.dependencies {
            implementation(compose.html.core)
//            implementation(libs.kotlinx.coroutines.js)
            implementation(libs.sqldelight.js)
            implementation(npm("kotlinx-coroutines-core", libs.versions.coroutines.get()))
            implementation(npm("sql.js", "1.8.0"))
            implementation(npm("dateformat", "4.0.2"))
            implementation(npm("@cashapp/sqldelight-sqljs-worker", libs.versions.sqldelight.get()))
            implementation(devNpm("copy-webpack-plugin", "11.0.0"))
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

android {
    namespace = "illyan.butler"
    compileSdk = 34
    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    defaultConfig {
        minSdk = 26
    }
    version = libs.versions.butler.get()
}

sqldelight {
    databases {
        create("Database") {
            packageName = "illyan.butler.db"
            generateAsync = true
        }
    }
}

//libres {
//    generatedClassName = "Res" // "Res" by default
//    generateNamedArguments = true // false by default
//    baseLocaleLanguageCode = "en" // "en" by default
//    camelCaseNamesForAppleFramework = false // false by default
//}

kotlin.sourceSets.all {
    languageSettings.optIn("kotlin.experimental.ExperimentalObjCName")
}

buildConfig {
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

    buildConfigField(
        "String",
        "FIREBASE_WEB_AND_DESKTOP_API_KEY",
        "\"$firebaseWebAndDesktopApiKey\""
    )
    buildConfigField("String", "FIREBASE_MESSAGING_SENDER_ID", "\"$firebaseMessagingSenderId\"")
    buildConfigField("String", "FIREBASE_DESKTOP_APP_ID", "\"$firebaseDesktopAppId\"")
    buildConfigField("String", "FIREBASE_WEB_APP_ID", "\"$firebaseWebAppId\"")
    buildConfigField("String", "FIREBASE_STORAGE_BUCKET", "\"$firebaseStorageBucket\"")
    buildConfigField("String", "FIREBASE_PROJECT_ID", "\"$firebaseProjectId\"")
    buildConfigField("String", "FIREBASE_AUTH_DOMAIN", "\"$firebaseAuthDomain\"")
}
