import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

@OptIn(ExperimentalWasmDsl::class, ExperimentalKotlinGradlePluginApi::class)
internal fun configureKotlinMultiplatform(
    extension: KotlinMultiplatformExtension
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
}
