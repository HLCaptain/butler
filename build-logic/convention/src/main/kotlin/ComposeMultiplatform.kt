import org.gradle.kotlin.dsl.getByType
import org.jetbrains.compose.ComposeExtension
import org.jetbrains.compose.ComposePlugin
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

internal fun configureComposeMultiplatformLibrary(
    extension: KotlinMultiplatformExtension
) = configureComposeMultiplatform(extension, extension.extensions.getByType())

internal fun configureComposeMultiplatform(
    extension: KotlinMultiplatformExtension
) = configureComposeMultiplatform(extension, extension.extensions.getByType<ComposeExtension>().dependencies)

private fun configureComposeMultiplatform(
    extension: KotlinMultiplatformExtension,
    compose: ComposePlugin.Dependencies
) = extension.apply {
    sourceSets.commonMain.dependencies {
        implementation(compose.runtime)
        implementation(compose.runtimeSaveable)
        implementation(compose.ui)
        implementation(compose.foundation)
        implementation(compose.materialIconsExtended)
        implementation(compose.material3)
        implementation(compose.components.resources)
        implementation(compose.components.uiToolingPreview)
        implementation(compose.preview)
        implementation(compose.uiTooling)
        implementation(compose.uiUtil)

        implementation(project.defaultLibs.findLibrary("jetbrains.lifecycle.viewmodel.compose").get().get())
        implementation(project.defaultLibs.findLibrary("jetbrains.navigation.compose").get().get())
    }

    sourceSets.androidMain.dependencies {
        implementation(compose.preview)
    }
}
