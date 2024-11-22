import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

class KoinForKotlinMultiplatformPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        with(pluginManager) {
            apply(defaultLibs.findPlugin("google.ksp").get().get().pluginId)
        }

        try {
            extensions.configure<KotlinMultiplatformExtension>(::configureKoinForKotlinMultiplatform)
        } catch (e: Exception) {
            logger.error("KotlinMultiplatformExtension not found. Please include ${KotlinMultiplatformLibraryPlugin::class.qualifiedName} in your build script.")
        }

        dependencies {
            val koinKsp = defaultLibs.findLibrary("koin.ksp").get().get()
            add("kspCommonMainMetadata", koinKsp)
            add("kspAndroid", koinKsp)
            add("kspJvm", koinKsp)
        }
    }
}