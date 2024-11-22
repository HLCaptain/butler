import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Project

internal fun Project.configureKotlinAndroidLibrary(
    extension: LibraryExtension
) = extension.apply {
    // get module name from module path
    // e.g. ":composeApp:feature:home" -> "feature.home"
    val moduleName = path.split(":").drop(2).joinToString(".")
    namespace = if (moduleName.isNotEmpty()) "illyan.butler.$moduleName" else "illyan.butler"

    compileSdk = defaultLibs.findVersion("android.compileSdk").get().requiredVersion.toInt()
    defaultConfig {
        minSdk = defaultLibs.findVersion("android.minSdk").get().requiredVersion.toInt()
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}
