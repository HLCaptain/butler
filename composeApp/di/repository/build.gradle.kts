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
        implementation(projects.composeApp.config)
        implementation(projects.composeApp.data.error)
        implementation(projects.composeApp.data.host)
        implementation(projects.composeApp.data.user)
        implementation(projects.composeApp.data.chat)
        implementation(projects.composeApp.data.model)
        implementation(projects.composeApp.data.message)
        implementation(projects.composeApp.data.resource)
        implementation(projects.composeApp.data.settings)
        implementation(projects.composeApp.data.permission)
        implementation(projects.composeApp.domain.error)

        implementation(projects.composeApp.core.network.ktor)
        implementation(projects.composeApp.core.local.room)

        api(project.dependencies.platform(libs.koin.bom))
        api(libs.koin.core)
        implementation(libs.koin.annotations)
        
        implementation(libs.koin.compose.viewmodel)
    }
}

dependencies {
    add("kspCommonMainMetadata", libs.koin.ksp)
    add("kspAndroid", libs.koin.ksp)
    add("kspJvm", libs.koin.ksp)
}

android {
    namespace = "illyan.butler.di.repository"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
}
