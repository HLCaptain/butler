package illyan.butler.domain.model

import kotlinx.serialization.Serializable

@Serializable
class DomainPreferences(
    val userId: String? = null,
    val analyticsEnabled: Boolean = false,
    val theme: Theme = Theme.System,
) {
    companion object {
        val Default = DomainPreferences()
    }
}