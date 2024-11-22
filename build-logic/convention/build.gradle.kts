plugins {
    `kotlin-dsl`
}

group = "illyan.butler.build-logic"

kotlin {
    jvmToolchain(21)
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

gradlePlugin {
    plugins {
        register("kotlinMultiplatform") {
            id = "illyan.butler.kotlinMultiplatform"
            implementationClass = "KotlinMultiplatformPlugin"
        }
        register("composeMultiplatform") {
            id = "illyan.butler.composeMultiplatform"
            implementationClass = "ComposeMultiplatformPlugin"
        }
        register("kotlinMultiplatformLibrary") {
            id = "illyan.butler.kotlinMultiplatformLibrary"
            implementationClass = "KotlinMultiplatformLibraryPlugin"
        }
        register("composeMultiplatformLibrary") {
            id = "illyan.butler.composeMultiplatformLibrary"
            implementationClass = "ComposeMultiplatformLibraryPlugin"
        }
        register("koinForKotlinMultiplatform") {
            id = "illyan.butler.koinForKotlinMultiplatform"
            implementationClass = "KoinForKotlinMultiplatformPlugin"
        }
        register("koinForComposeMultiplatform") {
            id = "illyan.butler.koinForComposeMultiplatform"
            implementationClass = "KoinForComposeMultiplatformPlugin"
        }
    }
}

dependencies {
    compileOnly(libs.android.gradlePlugin)
    compileOnly(libs.kotlin.gradlePlugin)
    compileOnly(libs.compose.gradlePlugin)
    compileOnly(libs.ksp.gradlePlugin)
}
