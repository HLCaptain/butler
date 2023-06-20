import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("multiplatform")
    id("org.jetbrains.compose")
}

group = "illyan"
version = "1.0-SNAPSHOT"

kotlin {
    jvm {
        jvmToolchain(17)
        withJava()
    }
    sourceSets {
        val jvmMain by getting {
            dependencies {
                implementation(project(":common"))
                implementation(compose.desktop.currentOs) {
                    exclude(libs.jetbrains.compose.material.desktop.get().group)
                }
                implementation(libs.jetbrains.compose.expui.theme)
                implementation(libs.mayakapps.compose.window.styler)
            }
        }
        val jvmTest by getting
    }
}

compose.desktop {
    application {
        mainClass = "MainKt"
        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "illyan.butler"
            packageVersion = "1.0.0"
            modules("jdk.unsupported")
        }
    }
}
