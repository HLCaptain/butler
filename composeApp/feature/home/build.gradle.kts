import org.jetbrains.compose.internal.utils.localPropertiesFile
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.jetbrains.compose)
    alias(libs.plugins.google.ksp)
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_21)
        }
    }
    jvm()

    sourceSets.commonMain.dependencies {
        implementation(projects.composeApp.core.ui.components)
        implementation(projects.composeApp.core.ui.resources)
        implementation(projects.composeApp.core.utils)
        implementation(projects.composeApp.core.ui.utils)
        implementation(projects.composeApp.domain)
        implementation(projects.composeApp.domain.config)
        implementation(projects.composeApp.domain.error)
        implementation(projects.composeApp.domain.auth)
        implementation(projects.composeApp.domain.permission)
        implementation(projects.composeApp.feature.auth)
        implementation(projects.composeApp.feature.chat)
        implementation(projects.composeApp.feature.onboarding)
        implementation(projects.composeApp.feature.profile)
        implementation(projects.composeApp.feature.error)
        implementation(projects.composeApp.feature.permission)

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

        api(project.dependencies.platform(libs.koin.bom))
        api(libs.koin.core)
        implementation(libs.koin.annotations)
        implementation(libs.koin.compose)
        implementation(libs.koin.compose.viewmodel)

        implementation(libs.kotlinx.coroutines)
        implementation(libs.kotlinx.datetime)

        implementation(libs.jetbrains.lifecycle.viewmodel.compose)
        implementation(libs.jetbrains.navigation.compose)

        implementation(libs.napier)
    }

    sourceSets.androidMain.dependencies {
        implementation(libs.androidx.appcompat)
        implementation(libs.androidx.activity)
    }
}

ksp {
    arg("KOIN_USE_COMPOSE_VIEWMODEL", "true")
}

dependencies {
    add("kspCommonMainMetadata", libs.koin.ksp)
    add("kspAndroid", libs.koin.ksp)
    add("kspJvm", libs.koin.ksp)
}

val keyStorePath = "${rootProject.projectDir}/composeApp"

val localProperties = File("${rootProject.projectDir.path}/local.properties").readLines().associate {
    if (it.startsWith("#") || !it.contains("=")) return@associate "" to ""
    val (key, value) = it.split("=", limit = 2)
    key to value
}

android {
    namespace = "illyan.butler.ui.home"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
    signingConfigs {
        val debugStorePath = "$keyStorePath/${localProperties["DEBUG_KEY_PATH"].toString()}"
        val debugKeyAlias = localProperties["DEBUG_KEY_ALIAS"].toString()
        val debugStorePassword = localProperties["DEBUG_KEYSTORE_PASSWORD"].toString()
        val debugKeyPassword = localProperties["DEBUG_KEY_PASSWORD"].toString()
        getByName("debug") {
            storeFile = file(debugStorePath)
            keyAlias = debugKeyAlias
            storePassword = debugStorePassword
            keyPassword = debugKeyPassword
        }
        val releaseStorePath = "$keyStorePath/${localProperties["RELEASE_KEY_PATH"].toString()}"
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
}
