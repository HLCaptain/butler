import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.jetbrains.compose)
}

dependencies {
    implementation(project(":common"))
    implementation(compose.desktop.currentOs)
//    implementation(libs.kotlinx.coroutines.swing)
}

compose.desktop.application {
    mainClass = "illyan.butler.MainKt"
    nativeDistributions {
        targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
        packageName = "desktop"
        packageVersion = libs.versions.butler.get().takeWhile { it != '-' }
    }
}
