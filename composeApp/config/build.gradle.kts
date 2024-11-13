import org.jetbrains.compose.internal.utils.localPropertiesFile
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.buildconfig)
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_21)
        }
    }
    jvm()

    sourceSets.commonMain.dependencies {

    }
}

android {
    namespace = "illyan.butler.config"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
}

val localProperties = rootProject.findProject(projects.composeApp.identityPath.path)!!.localPropertiesFile.readLines().associate {
    if (it.startsWith("#") || !it.contains("=")) return@associate "" to ""
    val (key, value) = it.split("=", limit = 2)
    key to value
}

buildConfig {
    // Setting required for collision avoidance with Android platform BuildConfig
    packageName = "illyan.butler.config"
    useKotlinOutput { internalVisibility = false }

    // Checking if the task is a debug or release task to set DEBUG flag
    // Parsing main task name to check if it contains debug or release type names
    // Not 100% accurate but should work for most cases
    // Defaults to DEBUG = true
    gradle.taskGraph.whenReady {
        val taskName = allTasks.last().name
        val debugIndicatorNames = listOf("dev", "debug")
        val prodIndicatorNames = listOf("release", "prod")
        val isProd = debugIndicatorNames.none { taskName.contains(it, ignoreCase = true) } &&
                prodIndicatorNames.any { taskName.contains(it, ignoreCase = true) }

        println("Task [$taskName] isProd=$isProd")
        buildConfigField("Boolean", "DEBUG", (!isProd).toString())

        val useMemoryDb = localProperties["USE_MEMORY_DB"].toBoolean() // Set to false to use Room database and Ktor, else memory based DB will be used without networking
        buildConfigField("Boolean", "USE_MEMORY_DB", if (isProd) "false" else useMemoryDb.toString())

        val resetRoomDb = localProperties["RESET_ROOM_DB"].toBoolean() // Set to true to reset Room database on app start
        buildConfigField("Boolean", "RESET_ROOM_DB", resetRoomDb.toString())
    }
}
