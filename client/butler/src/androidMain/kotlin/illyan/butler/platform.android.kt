package illyan.butler

actual fun getPlatformName(): String {
    return "Android"
}

actual fun isDebugBuild() = BuildConfig.DEBUG
