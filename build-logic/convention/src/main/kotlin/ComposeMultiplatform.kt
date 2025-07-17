import org.gradle.kotlin.dsl.getByType
import org.jetbrains.compose.ComposePlugin
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

internal fun configureComposeMultiplatformLibrary(
    extension: KotlinMultiplatformExtension
) = configureComposeMultiplatform(extension, extension.extensions.getByType())

internal fun configureComposeMultiplatform(
    extension: KotlinMultiplatformExtension
) = configureComposeMultiplatform(extension, extension.extensions.getByType())

@OptIn(ExperimentalWasmDsl::class, ExperimentalKotlinGradlePluginApi::class)
private fun configureComposeMultiplatform(
    extension: KotlinMultiplatformExtension,
    compose: ComposePlugin.Dependencies
) = extension.apply {
    jvmToolchain(21)

    applyDefaultHierarchyTemplate {
        common {
            group("ios") {
                withIosX64()
                withIosArm64()
                withIosSimulatorArm64()
            }

            group("nonWeb") {
                withJvm()
                withAndroidTarget()
                group("ios")
            }

            group("web") {
                withWasmJs()
                withJs()
            }

            group("nonMobile") {
                withJvm()
                group("web")
            }

            group("nonAndroid") {
                group("nonMobile")
                group("ios")
            }
        }
    }

    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_21)
        }
    }

    iosX64()
    iosArm64()
    iosSimulatorArm64()

    jvm()

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        nodejs()
    }

    js {
        nodejs()
    }

    sourceSets.commonMain.dependencies {
        implementation(compose.runtime)
        implementation(compose.runtimeSaveable)
        implementation(compose.ui)
        implementation(compose.foundation)
        implementation(compose.materialIconsExtended)
        implementation(compose.material3)
        implementation(compose.components.resources)

        implementation(project.defaultLibs.findLibrary("jetbrains.lifecycle.viewmodel.compose").get().get())
        implementation(project.defaultLibs.findLibrary("jetbrains.navigation.compose").get().get())
    }

    sourceSets.androidMain.dependencies {
        implementation(compose.preview)
        implementation(compose.uiTooling)
        implementation(compose.uiUtil)
        implementation(compose.components.uiToolingPreview)
    }
}
