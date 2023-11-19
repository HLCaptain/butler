plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.jetbrains.compose)
    application
}

dependencies {
    implementation(project(":common"))
    implementation(compose.desktop.currentOs)
    implementation(libs.kotlinx.coroutines.swing)
}

application {
    mainClass = "illyan.butler.MainKt"
    version = project.version.toString()
}
