package theme

import androidx.compose.runtime.ProvidedValue
import androidx.compose.ui.graphics.Color
import io.kanro.compose.jetbrains.expui.style.LocalAreaColors
import io.kanro.compose.jetbrains.expui.theme.DarkTheme
import io.kanro.compose.jetbrains.expui.theme.Theme
import io.kanro.compose.jetbrains.expui.window.LocalMainToolBarColors

object ButlerDarkThemeInverted : Theme {
    override val isDark: Boolean
        get() = DarkTheme.isDark

    override fun provideValues(): Array<ProvidedValue<*>> {
        return DarkTheme.provideValues() + arrayOf(
            LocalMainToolBarColors provides DarkTheme.MainToolBarColors.copy(
                normalAreaColors = DarkTheme.MainToolBarColors.normalAreaColors.copy(
                    startBackground = Color.Black.copy(alpha = 0.4f),
                    endBackground = Color.Black.copy(alpha = 0.4f),
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