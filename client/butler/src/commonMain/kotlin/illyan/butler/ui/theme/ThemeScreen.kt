package illyan.butler.ui.theme

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import illyan.butler.domain.model.Theme
import io.github.aakira.napier.Napier

class ThemeScreen(private val content: @Composable () -> Unit) : Screen {
    @Composable
    override fun Content() {
        val screenModel = koinScreenModel<ThemeScreenModel>()
        ButlerTheme(
            themeState = screenModel.theme.collectAsState(),
            dynamicColorEnabledState = screenModel.dynamicColorEnabled.collectAsState(),
            isNightState = screenModel.isNight.collectAsState(),
            content = content
        )
    }
}

@Composable
fun ButlerTheme(
    themeState: State<Theme?> = mutableStateOf(Theme.System),
    dynamicColorEnabledState: State<Boolean> = mutableStateOf(true),
    isNightState: State<Boolean> = mutableStateOf(false),
    content: @Composable () -> Unit,
) {
    val theme by themeState
    val dynamicColorEnabled by dynamicColorEnabledState
    val isNight by isNightState
    val isSystemInDarkTheme = isSystemInDarkTheme()
    var isDark by rememberSaveable { mutableStateOf<Boolean?>(null) }
    LaunchedEffect(theme, dynamicColorEnabled, isNight) {
        isDark = when (theme) {
            Theme.Light -> false
            Theme.Dark -> true
            Theme.System -> isSystemInDarkTheme
            Theme.DayNightCycle -> isNight
            null -> null
        }
    }
    val dynamicLightColorScheme = dynamicDarkColorScheme()
    val dynamicDarkColorScheme = dynamicLightColorScheme()
    val dynamicLightColorSchemeState by remember { derivedStateOf { dynamicLightColorScheme } }
    val dynamicDarkColorSchemeState by remember { derivedStateOf { dynamicDarkColorScheme } }
    val targetColorScheme by remember {
        derivedStateOf {
            if (dynamicColorEnabled && canUseDynamicColors()) {
                when (theme) {
                    Theme.Dark -> dynamicDarkColorSchemeState
                    Theme.Light -> dynamicLightColorSchemeState
                    Theme.System -> if (isSystemInDarkTheme) dynamicDarkColorSchemeState else dynamicLightColorSchemeState
                    Theme.DayNightCycle -> if (isNight) dynamicDarkColorSchemeState else dynamicLightColorSchemeState
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
    }

    LaunchedEffect(targetColorScheme) {
        val themeName = when (targetColorScheme) {
            LightColors -> "Light"
            DarkColors -> "Dark"
            dynamicLightColorScheme -> "Dynamic Light"
            dynamicDarkColorScheme -> "Dynamic Dark"
            else -> "Undefined theme"
        }
        Napier.d("Theme: $theme, Dynamic colors: ${dynamicColorEnabled && canUseDynamicColors()}, Is night: $isNight, Is system in dark theme: $isSystemInDarkTheme, Is dark: $isDark, Target color scheme: $themeName")
    }

    ThemeSystemWindow(isDark ?: isSystemInDarkTheme, dynamicColorEnabled && canUseDynamicColors())

    val colorScheme by animateColorScheme(targetColorScheme, spring(stiffness = Spring.StiffnessLow))
    CompositionLocalProvider(LocalTheme provides theme) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = MaterialTheme.typography,
            content = content
        )
    }
}