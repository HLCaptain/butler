package illyan.butler.core.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.materialkolor.Contrast
import com.materialkolor.DynamicMaterialTheme
import com.materialkolor.PaletteStyle
import com.materialkolor.rememberDynamicMaterialThemeState
import illyan.butler.domain.model.Theme

@Composable
fun ButlerTheme(
    theme: Theme = Theme.System,
    paletteStyle: PaletteStyle = PaletteStyle.Expressive,
    contrast: Contrast = Contrast.Default,
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
        }
    }
    val dynamicLightColorScheme = dynamicLightColorScheme()
    val dynamicDarkColorScheme = dynamicDarkColorScheme()
    val canUseDynamicColors = canUseDynamicColors()
    val targetColorScheme = remember(theme, dynamicColorEnabled, isNight, isSystemInDarkTheme, isDark) {
        if (dynamicColorEnabled && canUseDynamicColors) {
            when (theme) {
                Theme.Dark -> dynamicDarkColorScheme
                Theme.Light -> dynamicLightColorScheme
                Theme.System -> if (isSystemInDarkTheme) dynamicDarkColorScheme else dynamicLightColorScheme
                Theme.DayNightCycle -> if (isNight) dynamicDarkColorScheme else dynamicLightColorScheme
            }
        } else {
            when (theme) {
                Theme.Dark -> DarkColors
                Theme.Light -> LightColors
                Theme.System -> if (isSystemInDarkTheme) DarkColors else LightColors
                Theme.DayNightCycle -> if (isNight) DarkColors else LightColors
            }
        }
    }

    ThemeSystemWindow(isDark, dynamicColorEnabled)

    val themeState = rememberDynamicMaterialThemeState(
        primary = targetColorScheme.primary,
        secondary = targetColorScheme.secondary,
        tertiary = targetColorScheme.tertiary,
        neutral = targetColorScheme.surface,
        neutralVariant = targetColorScheme.surfaceVariant,
        error = targetColorScheme.error,
        contrastLevel = contrast.value,
        isDark = isDark,
        style = paletteStyle,
    )
    DynamicMaterialTheme(
        state = themeState,
        typography = MaterialTheme.typography,
        shapes = butlerShapes(),
        animate = true,
        content = content
    )
}
