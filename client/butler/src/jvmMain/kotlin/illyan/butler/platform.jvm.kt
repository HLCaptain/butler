package illyan.butler

import illyan.butler.config.BuildConfig

actual fun getPlatformName(): String {
    return "JVM"
}

actual fun isDebugBuild() = BuildConfig.DEBUG
