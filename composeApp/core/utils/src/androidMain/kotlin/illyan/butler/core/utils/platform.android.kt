package illyan.butler.core.utils

import android.os.Build

actual fun getSystemMetadata(): Map<String, String> {
    return mapOf(
        "Build.VERSION.SDK_INT" to Build.VERSION.SDK_INT.toString(),
        "Build.VERSION.RELEASE" to Build.VERSION.RELEASE,
        "Build.VERSION.CODENAME" to Build.VERSION.CODENAME,
        "Build.VERSION.INCREMENTAL" to Build.VERSION.INCREMENTAL,
        "Build.VERSION.BASE_OS" to Build.VERSION.BASE_OS,
        "Build.VERSION.PREVIEW_SDK_INT" to Build.VERSION.PREVIEW_SDK_INT.toString(),
        "Build.VERSION.SECURITY_PATCH" to Build.VERSION.SECURITY_PATCH
    )
}

actual fun getPlatformName(): String {
    return "Android"
}

actual fun getOsName(): String {
    return Build.VERSION.SDK_INT.toString()
}