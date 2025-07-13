package illyan.butler.domain.model

import com.materialkolor.scheme.Variant
import kotlinx.serialization.Serializable

@Serializable
data class Theming(
    val theme: Theme = Theme.System,
    val paletteVariant: Variant = Variant.EXPRESSIVE,
    val contrast: Double = 0.0,
    val themeColor: ThemeColor = ThemeColor.Default,
    val dynamicColorEnabled: Boolean = true,
) {
    companion object {
        val Default = Theming()
    }
}
