package illyan.butler.core.ui.theme

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import illyan.butler.core.ui.theme.ThemeSystemWindow
import illyan.butler.core.ui.theme.canUseDynamicColors
import illyan.butler.core.ui.theme.dynamicDarkColorScheme
import illyan.butler.core.ui.theme.dynamicLightColorScheme
import illyan.butler.domain.model.Theme

@Composable
fun ButlerTheme(
    theme: Theme? = null,
    dynamicColorEnabled: Boolean = false,
    isNight: Boolean = true,
    content: @Composable () -> Unit,
) {
    val isSystemInDarkTheme = isSystemInDarkTheme()
    val isDark = remember(theme, isNight, isSystemInDarkTheme) {
        when (theme) {
            Theme.Light -> false
            Theme.Dark -> true
            Theme.System -> isSystemInDarkTheme
            Theme.DayNightCycle -> isNight
            null -> null
        }
    }
    val dynamicLightColorScheme = dynamicLightColorScheme()
    val dynamicDarkColorScheme = dynamicDarkColorScheme()
    val targetColorScheme = remember(theme, dynamicColorEnabled, isNight, isSystemInDarkTheme, isDark) {
        if (dynamicColorEnabled && canUseDynamicColors()) {
            when (theme) {
                Theme.Dark -> dynamicDarkColorScheme
                Theme.Light -> dynamicLightColorScheme
                Theme.System -> if (isSystemInDarkTheme) dynamicDarkColorScheme else dynamicLightColorScheme
                Theme.DayNightCycle -> if (isNight) dynamicDarkColorScheme else dynamicLightColorScheme
                null -> LightColors
            }
        } else {
            when (theme) {
                Theme.Dark -> DarkColors
                Theme.Light -> LightColors
                Theme.System -> if (isSystemInDarkTheme) DarkColors else LightColors
                Theme.DayNightCycle -> if (isNight) DarkColors else LightColors
                null -> LightColors
            }
        }
    }

    ThemeSystemWindow(isDark ?: isSystemInDarkTheme, dynamicColorEnabled)

    val colorScheme by animateColorScheme(targetColorScheme, spring(stiffness = Spring.StiffnessLow))
    MaterialTheme(
        colorScheme = colorScheme,
        typography = MaterialTheme.typography,
        content = content
    )
}