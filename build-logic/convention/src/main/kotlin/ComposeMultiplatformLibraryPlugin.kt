import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

class ComposeMultiplatformLibraryPlugin : KotlinMultiplatformLibraryPlugin() {
    override fun apply(target: Project) = with(target) {
        super.apply(target)
        with(pluginManager) {
            apply(defaultLibs.findPlugin("compose.compiler").get().get().pluginId)
            apply(defaultLibs.findPlugin("jetbrains.compose").get().get().pluginId)
        }
        extensions.configure<KotlinMultiplatformExtension>(::configureComposeMultiplatformLibrary)
        extensions.configure<LibraryExtension> { buildFeatures { compose = true } }
    }
}
