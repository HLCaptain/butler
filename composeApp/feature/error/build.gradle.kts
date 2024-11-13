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
        implementation(projects.composeApp.domain)
        implementation(projects.composeApp.domain.error)

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

        implementation(libs.ktor.core)
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

android {
    namespace = "illyan.butler.ui.error"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
}
