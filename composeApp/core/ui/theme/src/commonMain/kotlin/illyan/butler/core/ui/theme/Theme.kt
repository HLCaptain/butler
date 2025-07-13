package illyan.butler.core.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import com.materialkolor.Contrast
import com.materialkolor.DynamicMaterialTheme
import com.materialkolor.PaletteStyle
import com.materialkolor.rememberDynamicMaterialThemeState
import illyan.butler.domain.model.Theme
import illyan.butler.domain.model.ThemeColor

@Composable
fun ButlerTheme(
    theme: Theme = Theme.System,
    paletteStyle: PaletteStyle = PaletteStyle.Expressive,
    contrast: Contrast = Contrast.Default,
    themeColor: ThemeColor = ThemeColor.Default,
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
        seedColor = themeColor.seedArgb?.let { Color(it) } ?: targetColorScheme.primary,
        primary = if (themeColor.seedArgb != null) null else themeColor.primaryArgb?.let { Color(it) } ?: targetColorScheme.primary,
        secondary = if (themeColor.seedArgb != null) null else themeColor.secondaryArgb?.let { Color(it) } ?: targetColorScheme.secondary,
        tertiary = if (themeColor.seedArgb != null) null else themeColor.tertiaryArgb?.let { Color(it) } ?: targetColorScheme.tertiary,
        neutral = if (themeColor.seedArgb != null) null else themeColor.neutralArgb?.let { Color(it) } ?: targetColorScheme.surface,
        neutralVariant = if (themeColor.seedArgb != null) null else themeColor.neutralVariantArgb?.let { Color(it) } ?: targetColorScheme.surfaceVariant,
        error = if (themeColor.seedArgb != null) null else themeColor.errorArgb?.let { Color(it) } ?: targetColorScheme.error,
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
