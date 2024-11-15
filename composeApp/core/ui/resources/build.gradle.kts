plugins {
    alias(libs.plugins.illyan.butler.composeMultiplatformLibrary)
}

compose.resources {
    publicResClass = true // Make Res accessible from other modules
    packageOfResClass = "illyan.butler.generated.resources"
    generateResClass = always // To use Res class transitively
}
