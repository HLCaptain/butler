plugins {
    kotlin("multiplatform")
    id("com.android.library")
}

@OptIn(org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi::class)
kotlin {
    targetHierarchy.default()

    jvm()

    android {
        compilations.all {
            kotlinOptions {
                jvmTarget = "17"
            }
        }
    }

    js(IR) {
        browser()
        nodejs()
    }
    
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach {
        it.binaries.framework {
            baseName = "shared"
        }
    }

    linuxX64()
    macosX64()
    macosArm64()
    mingwX64()

    sourceSets {
        val commonMain by getting {
            dependencies {
                //put your multiplatform dependencies here
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
        val androidMain by getting
        val jsMain by getting
        val windowsMain by creating
        val mingwX64Main by getting
    }
}

android {
    namespace = "illyan.butler.shared"
    compileSdk = 33
    defaultConfig {
        minSdk = 24
    }
}