package illyan.butler.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Preferences(
    val userId: String? = null,
    val analyticsEnabled: Boolean = false,
    val theming: Theming = Theming.Default,
) {
    companion object {
        val Default = Preferences()
    }
}
