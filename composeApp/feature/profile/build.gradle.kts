import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask

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

    sourceSets.commonMain.configure { kotlin.srcDir("build/generated/ksp/metadata/commonMain/kotlin") }

    sourceSets.commonMain.dependencies {
        implementation(projects.composeApp.core.ui.components)
        implementation(projects.composeApp.core.ui.resources)
        implementation(projects.composeApp.core.utils)
        implementation(projects.composeApp.feature.theme)
        implementation(projects.composeApp.domain.auth)
        implementation(projects.composeApp.domain.config)
        implementation(projects.composeApp.domain)
        implementation(projects.composeApp.domain.settings)

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
        api(libs.koin.core.viewmodel)
        api(libs.koin.compose.viewmodel)

        implementation(libs.kotlinx.coroutines)
        implementation(libs.kotlinx.datetime)

        implementation(libs.jetbrains.lifecycle.viewmodel.compose)

        implementation(libs.napier)
    }

    sourceSets.androidMain.dependencies {
        implementation(libs.androidx.appcompat)
        implementation(libs.androidx.activity)
        implementation(libs.koin.android)
    }

    sourceSets.jvmMain.dependencies {

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

// Trigger Common Metadata Generation from Native tasks
project.tasks.withType(KotlinCompilationTask::class.java).configureEach {
    if(name != "kspCommonMainKotlinMetadata") {
        dependsOn("kspCommonMainKotlinMetadata")
    }
}

android {
    namespace = "illyan.butler.ui.theme"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
}
