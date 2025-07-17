package illyan.butler.core.utils

actual val userAgent: String = js("navigator.userAgent")
actual val platform: String = js("navigator.platform")
actual val language: String = js("navigator.language")
