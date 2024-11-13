import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.multiplatform)
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
        implementation(projects.composeApp.core.local)
        implementation(projects.composeApp.core.local.room)
        implementation(projects.composeApp.domain)
        implementation(projects.shared)

        api(project.dependencies.platform(libs.koin.bom))
        api(libs.koin.core)
        implementation(libs.koin.annotations)

        implementation(libs.kotlinx.coroutines)
        implementation(libs.napier)
    }

    sourceSets.androidMain.dependencies {
        implementation(libs.androidx.appcompat)
        implementation(libs.androidx.activity)
    }
}

dependencies {
    add("kspCommonMainMetadata", libs.koin.ksp)
    add("kspAndroid", libs.koin.ksp)
    add("kspJvm", libs.koin.ksp)
}

android {
    namespace = "illyan.butler.data.permission"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
}
