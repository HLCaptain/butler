import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

internal fun configureKoinForKotlinMultiplatform(
    extension: KotlinMultiplatformExtension
) = extension.apply {
    sourceSets.commonMain {
        kotlin.srcDir("build/generated/ksp/metadata/commonMain/kotlin")
        dependencies {
            implementation(project.dependencies.platform(project.defaultLibs.findLibrary("koin.bom").get().get()))
            implementation(project.defaultLibs.findLibrary("koin.core").get().get())
            implementation(project.defaultLibs.findLibrary("koin.annotations").get().get())
        }
    }
}
