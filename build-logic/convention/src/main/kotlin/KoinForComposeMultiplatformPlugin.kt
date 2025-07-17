import com.google.devtools.ksp.gradle.KspAATask
import com.google.devtools.ksp.gradle.KspExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask

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
//            add("kspAndroid", koinKsp)
//            add("kspJvm", koinKsp)
//            add("kspWasmJs", koinKsp)
//            add("kspJs", koinKsp)
//            add("kspIosX64", koinKsp)
//            add("kspIosArm64", koinKsp)
//            add("kspIosSimulatorArm64", koinKsp)
        }

        // WORKAROUND: ADD this dependsOn("kspCommonMainKotlinMetadata") instead of above dependencies
        // (https://github.com/InsertKoinIO/hello-kmp/blob/annotations/shared/build.gradle.kts)
        tasks.withType<KotlinCompilationTask<*>>().configureEach {
            if (name != "kspCommonMainKotlinMetadata") {
                dependsOn("kspCommonMainKotlinMetadata")
            }
        }
        project.tasks.withType<KspAATask>().configureEach {
            if (name != "kspCommonMainKotlinMetadata") {
//                val tasks = listOf(
//                    "kspDebugKotlinAndroid",
//                    "kspReleaseKotlinAndroid",
//                    "kspKotlinIosSimulatorArm64",
//                    "kspKotlinIosX64",
//                    "kspKotlinIosArm64",
//                    "kspKotlinWasmJs",
//                    "kspKotlinJs",
//                    "kspKotlinJvm"
//                )
//                if (tasks.any { name.contains(it, ignoreCase = true) }) {
//                    enabled = false
//                }
                dependsOn("kspCommonMainKotlinMetadata")
            }
        }
//        afterEvaluate {
//            tasks.filter {
//                it.name.contains("SourcesJar", true)
//            }.forEach {
//                println("SourceJarTask====>${it.name}")
//                it.dependsOn("kspCommonMainKotlinMetadata")
//            }
//        }

        extensions.configure<KspExtension> {
            arg("KOIN_USE_COMPOSE_VIEWMODEL", "true")
            arg("KOIN_DEFAULT_MODULE", "false")
        }
    }
}
