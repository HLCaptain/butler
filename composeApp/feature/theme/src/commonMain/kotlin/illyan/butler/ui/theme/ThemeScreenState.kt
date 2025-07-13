package illyan.butler.ui.theme

import com.materialkolor.Contrast
import com.materialkolor.PaletteStyle
import illyan.butler.domain.model.Theme
import illyan.butler.domain.model.ThemeColor

data class ThemeScreenState(
    val theme: Theme = Theme.System,
    val paletteStyle: PaletteStyle = PaletteStyle.Expressive,
    val contrast: Contrast = Contrast.Default,
    val dynamicColorEnabled: Boolean = false,
    val isNight: Boolean = true,
    val themeColor: ThemeColor = ThemeColor.Default
)
