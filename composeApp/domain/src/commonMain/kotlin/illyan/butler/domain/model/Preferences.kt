package illyan.butler.domain.model

import com.materialkolor.scheme.Variant
import kotlinx.serialization.Serializable

@Serializable
data class Preferences(
    val userId: String? = null,
    val analyticsEnabled: Boolean = false,
    val dynamicColorEnabled: Boolean = true,
    val theme: Theme = Theme.System,
    val paletteVariant: Variant = Variant.EXPRESSIVE,
    val contrast: Double = 0.0,
) {
    companion object {
        val Default = Preferences()
    }
}
