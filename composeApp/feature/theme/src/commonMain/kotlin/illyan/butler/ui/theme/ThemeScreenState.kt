package illyan.butler.ui.theme

import illyan.butler.domain.model.Theme

data class ThemeScreenState(
    val theme: Theme? = null,
    val dynamicColorEnabled: Boolean = false,
    val isNight: Boolean = true,
)
