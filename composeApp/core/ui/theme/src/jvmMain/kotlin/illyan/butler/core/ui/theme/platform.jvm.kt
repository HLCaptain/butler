package illyan.butler.core.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable

@Composable
actual fun ThemeSystemWindow(isDark: Boolean, isDynamicColors: Boolean) {
}

actual fun canUseDynamicColors(): Boolean {
    return false
}

@Composable
actual fun dynamicDarkColorScheme(): ColorScheme {
    return DarkColors // No dynamic colors on JVM
}

@Composable
actual fun dynamicLightColorScheme(): ColorScheme {
    return LightColors // No dynamic colors on JVM
}
