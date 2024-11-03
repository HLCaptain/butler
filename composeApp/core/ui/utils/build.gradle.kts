import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.jetbrains.compose)
}

kotlin {
    androidTarget {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_21)
        }
    }
    jvm()

    sourceSets.commonMain.dependencies {
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
    }
}

android {
    namespace = "illyan.butler.core.ui.utils"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
}
