package theme

import androidx.compose.runtime.ProvidedValue
import androidx.compose.ui.graphics.Color
import io.kanro.compose.jetbrains.expui.style.LocalAreaColors
import io.kanro.compose.jetbrains.expui.theme.DarkTheme
import io.kanro.compose.jetbrains.expui.theme.LightTheme
import io.kanro.compose.jetbrains.expui.theme.Theme
import io.kanro.compose.jetbrains.expui.window.LocalMainToolBarColors

object ButlerDarkTheme : Theme {
    override val isDark: Boolean
        get() = DarkTheme.isDark

    override fun provideValues(): Array<ProvidedValue<*>> {
        return DarkTheme.provideValues() + arrayOf(
            LocalMainToolBarColors provides DarkTheme.MainToolBarColors.copy(
                normalAreaColors = DarkTheme.MainToolBarColors.normalAreaColors.copy(
                    startBackground = Color.White.copy(alpha = 0.0f),
                    endBackground = Color.White.copy(alpha = 0.0f),
                ),
                inactiveAreaColors = DarkTheme.MainToolBarColors.inactiveAreaColors.copy(
                    startBackground = Color.Transparent,
                    endBackground = Color.Transparent,
                ),
            ),
            LocalAreaColors provides DarkTheme.NormalAreaColors.copy(
                startBackground = Color.Transparent,
                endBackground = Color.Transparent,
                startBorderColor = Color.Transparent,
                endBorderColor = Color.Transparent,
            ),
        )
    }
}