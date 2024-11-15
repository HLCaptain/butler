import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

open class KotlinMultiplatformLibraryPlugin : KotlinMultiplatformPlugin() {
    override fun apply(target: Project) = with(target) {
        with(pluginManager) {
            apply(defaultLibs.findPlugin("android.library").get().get().pluginId)
        }
        extensions.configure<LibraryExtension>(::configureKotlinAndroidLibrary)
        super.apply(target)
    }
}
