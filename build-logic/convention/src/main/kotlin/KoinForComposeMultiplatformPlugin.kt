import com.google.devtools.ksp.gradle.KspExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

class KoinForComposeMultiplatformPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        with(pluginManager) {
            apply(defaultLibs.findPlugin("google.ksp").get().get().pluginId)
        }

        try {
            extensions.configure<KotlinMultiplatformExtension>(::configureKoinForComposeMultiplatform)
        } catch (e: Exception) {
            logger.error("KotlinMultiplatformExtension not found. Please include ${ComposeMultiplatformPlugin::class.qualifiedName} in your build script.")
        }

        dependencies {
            val koinKsp = defaultLibs.findLibrary("koin.ksp").get().get()
            add("kspCommonMainMetadata", koinKsp)
            add("kspAndroid", koinKsp)
            add("kspJvm", koinKsp)
        }

        extensions.configure<KspExtension> {
            arg("KOIN_USE_COMPOSE_VIEWMODEL", "true")
            arg("KOIN_DEFAULT_MODULE", "false")
        }
    }
}
