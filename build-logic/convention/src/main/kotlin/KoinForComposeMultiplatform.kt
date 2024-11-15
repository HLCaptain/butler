import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

internal fun configureKoinForComposeMultiplatform(
    extension: KotlinMultiplatformExtension
) = extension.apply {
    sourceSets.commonMain.dependencies {
        implementation(project.dependencies.platform(project.defaultLibs.findLibrary("koin.bom").get().get()))
        implementation(project.defaultLibs.findLibrary("koin.core").get().get())
        implementation(project.defaultLibs.findLibrary("koin.annotations").get().get())
        implementation(project.defaultLibs.findLibrary("koin.compose").get().get())
        implementation(project.defaultLibs.findLibrary("koin.compose.viewmodel").get().get())
    }
}