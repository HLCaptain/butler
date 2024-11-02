package illyan.butler.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable

@Composable
expect fun ThemeSystemWindow(isDark: Boolean, isDynamicColors: Boolean)

expect fun canUseDynamicColors(): Boolean

@Composable
expect fun dynamicDarkColorScheme(): ColorScheme

@Composable
expect fun dynamicLightColorScheme(): ColorScheme
