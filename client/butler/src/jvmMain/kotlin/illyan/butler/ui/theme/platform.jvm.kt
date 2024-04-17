package illyan.butler.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

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

@OptIn(ExperimentalComposeUiApi::class)
@Composable
actual fun getWindowSize(): Pair<Dp, Dp> {
    val density = LocalDensity.current.density
    val containerSize = LocalWindowInfo.current.containerSize
    return Pair(containerSize.width.dp / density, containerSize.height.dp / density)
}