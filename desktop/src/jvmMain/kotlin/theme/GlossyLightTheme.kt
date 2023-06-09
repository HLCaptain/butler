package theme

import androidx.compose.runtime.ProvidedValue
import androidx.compose.ui.graphics.Color
import io.kanro.compose.jetbrains.expui.style.LocalAreaColors
import io.kanro.compose.jetbrains.expui.theme.LightTheme
import io.kanro.compose.jetbrains.expui.theme.Theme
import io.kanro.compose.jetbrains.expui.window.LocalMainToolBarColors

object GlossyLightTheme : Theme {
    override val isDark: Boolean
        get() = LightTheme.isDark

    override fun provideValues(): Array<ProvidedValue<*>> {
        return LightTheme.provideValues() + arrayOf(
            LocalMainToolBarColors provides LightTheme.MainToolBarColors.copy(
                normalAreaColors = LightTheme.MainToolBarColors.normalAreaColors.copy(
                    text = LightTheme.NormalAreaColors.text,
                    startBackground = Color.Black.copy(alpha = 0.1f),
                    endBackground = Color.Black.copy(alpha = 0.1f),
                ),
                inactiveAreaColors = LightTheme.MainToolBarColors.inactiveAreaColors.copy(
                    text = LightTheme.NormalAreaColors.text,
                    startBackground = Color.Transparent,
                    endBackground = Color.Transparent,
                ),
            ),
            LocalAreaColors provides LightTheme.NormalAreaColors.copy(
                startBackground = Color.Transparent,
                endBackground = Color.Transparent,
                startBorderColor = Color.Transparent,
                endBorderColor = Color.Transparent,
            )
        )
    }
}