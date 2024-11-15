import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.getByType

// Do not rename "defaultLibs" to "libs" due to collision with
// generated files used in build.gradle.kts build scripts.
val Project.defaultLibs
    get(): VersionCatalog = extensions.getByType<VersionCatalogsExtension>().named("libs")
