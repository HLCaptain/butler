package illyan.butler.core.utils

val userAgent: String = js("navigator.userAgent")
val platform: String = js("navigator.platform")
val language: String = js("navigator.language")

actual fun getSystemMetadata(): Map<String, String> {
    return mapOf(
        "userAgent" to userAgent,
        "platform" to platform,
        "language" to language
    )
}

actual fun getPlatformName(): String {
    return "Web"
}

actual fun getOsName(): String {
    return when {
        userAgent.contains("Win") -> "Windows"
        userAgent.contains("Mac") -> "macOS"
        userAgent.contains("Linux") -> "Linux"
        userAgent.contains("Android") -> "Android"
        userAgent.contains("like Mac") -> "iOS"
        else -> "Unknown"
    }
}