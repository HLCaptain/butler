package illyan.butler

import illyan.butler.config.BuildConfig

actual fun getPlatformName(): String {
    return "Web"
}

actual fun isDebugBuild() = BuildConfig.DEBUG