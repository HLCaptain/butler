import org.jetbrains.kotlin.gradle.targets.js.yarn.YarnLockMismatchReport
import org.jetbrains.kotlin.gradle.targets.js.yarn.YarnRootExtension

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.jetbrains.compose)
}

//val copyJsResources = tasks.create("copyJsResourcesWorkaround", Copy::class.java) {
//    from(project(":common").file("src/commonMain/libres"))
//    into("build/processedResources/js/main")
//}
//
//afterEvaluate {
//    project.tasks.getByName("copyJsResourcesWorkaround").finalizedBy(copyJsResources)
//}

kotlin {
//    @OptIn(ExperimentalWasmDsl::class)
//    wasmJs {
//        moduleName = rootProject.name
//        browser {
//            commonWebpackConfig {
//                outputFileName = "web.js"
//            }
//        }
//        binaries.executable()
//    }
    js(IR) {
        moduleName = rootProject.name
        browser {
            commonWebpackConfig {
                outputFileName = "web.js"
            }
        }
    }
    sourceSets {
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.runtimeSaveable)
            implementation(compose.material)
            implementation(compose.material3)
            implementation(libs.napier)
        }
    }
}

compose.experimental {
    web.application {}
}

rootProject.plugins.withType(org.jetbrains.kotlin.gradle.targets.js.yarn.YarnPlugin::class.java) {
    rootProject.the<YarnRootExtension>().yarnLockMismatchReport = YarnLockMismatchReport.WARNING
    rootProject.the<YarnRootExtension>().reportNewYarnLock = true
    rootProject.the<YarnRootExtension>().yarnLockAutoReplace = true
}
