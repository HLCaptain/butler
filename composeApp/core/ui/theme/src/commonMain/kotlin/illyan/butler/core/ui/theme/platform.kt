package illyan.butler.core.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable

@Composable
expect fun ThemeSystemWindow(isDark: Boolean, isDynamicColors: Boolean)

@Composable
expect fun canUseDynamicColors(): Boolean

@Composable
expect fun dynamicDarkColorScheme(): ColorScheme

@Composable
expect fun dynamicLightColorScheme(): ColorScheme
