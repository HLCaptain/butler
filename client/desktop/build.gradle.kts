import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("multiplatform")
    id("org.jetbrains.compose")
}

group = "nest"
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
                val osName: String = System.getProperty("os.name")

                val targetOs = when {
                    osName == "Mac OS X" -> "macos"
                    osName.startsWith("Win") -> "windows"
                    osName.startsWith("Linux") -> "linux"
                    else -> error("Unsupported OS: $osName")
                }

                val targetArch = when (val osArch = System.getProperty("os.arch")) {
                    "x86_64", "amd64" -> "x64"
                    "aarch64" -> "arm64"
                    else -> error("Unsupported arch: $osArch")
                }

                val skikoVersion = "0.7.80" // or any more recent version
                val target = "${targetOs}-${targetArch}"
                implementation("org.jetbrains.skiko:skiko-awt-runtime-$target:$skikoVersion")
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
            packageName = "nest.butler"
            packageVersion = "1.0.0"
            modules("jdk.unsupported")
        }
    }
}
