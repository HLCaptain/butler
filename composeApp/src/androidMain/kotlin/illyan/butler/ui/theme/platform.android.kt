package illyan.butler.ui.theme

import android.annotation.SuppressLint
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

@SuppressLint("NewApi")
@Composable
actual fun ThemeSystemWindow(isDark: Boolean, isDynamicColors: Boolean) {
    val view = LocalView.current
    val activity = LocalContext.current as ComponentActivity
    val dynamicDarkColorScheme = dynamicDarkColorScheme()
    val dynamicLightColorScheme = dynamicLightColorScheme()
    val colorScheme = remember(isDark, isDynamicColors) {
        if (isDynamicColors && canUseDynamicColors()) {
            if (isDark) dynamicDarkColorScheme else dynamicLightColorScheme
        } else if (isDark) {
            DarkColors
        } else {
            LightColors
        }
    }
    if (!view.isInEditMode) {
        SideEffect {
            activity.window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(activity.window, view).isAppearanceLightStatusBars = !isDark
        }
        LaunchedEffect(colorScheme) {
            activity.enableEdgeToEdge(
                statusBarStyle = SystemBarStyle.auto(
                    android.graphics.Color.TRANSPARENT,
                    android.graphics.Color.TRANSPARENT,
                ) { isDark },
                navigationBarStyle = SystemBarStyle.auto(
                    colorScheme.background.toArgb(),
                    colorScheme.background.toArgb(),
                ) { isDark },
            )
        }
    }
}

actual fun canUseDynamicColors(): Boolean {
    return Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
}

@RequiresApi(Build.VERSION_CODES.S)
@Composable
actual fun dynamicDarkColorScheme(): ColorScheme {
    return if (canUseDynamicColors()) {
        dynamicDarkColorScheme(LocalContext.current)
    } else {
        DarkColors
    }
}

@RequiresApi(Build.VERSION_CODES.S)
@Composable
actual fun dynamicLightColorScheme(): ColorScheme {
    return if (canUseDynamicColors()) {
        dynamicLightColorScheme(LocalContext.current)
    } else {
        LightColors
    }
}
