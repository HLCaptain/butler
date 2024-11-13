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
        implementation(projects.composeApp.core.local.room)
        implementation(projects.composeApp.core.network)
        implementation(projects.composeApp.domain)
        implementation(projects.composeApp.config)
        implementation(projects.composeApp.data.settings)
        implementation(projects.composeApp.domain.error)
        implementation(projects.shared)

        api(project.dependencies.platform(libs.koin.bom))
        api(libs.koin.core)
        implementation(libs.koin.annotations)

        implementation(libs.ktor.core)
        implementation(libs.ktor.auth)
        implementation(libs.ktor.client.cio)
        implementation(libs.ktor.client.content.negotiation)
        implementation(libs.ktor.client.logging)
        implementation(libs.ktor.serialization.kotlinx.protobuf)
        implementation(libs.ktor.serialization.kotlinx.json)
        implementation(libs.ktor.client.encoding)

        implementation(libs.kotlinx.datetime)
        implementation(libs.napier)
    }
}

dependencies {
    add("kspCommonMainMetadata", libs.koin.ksp)
    add("kspAndroid", libs.koin.ksp)
    add("kspJvm", libs.koin.ksp)
}

android {
    namespace = "illyan.butler.core.network.ktor"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
}
