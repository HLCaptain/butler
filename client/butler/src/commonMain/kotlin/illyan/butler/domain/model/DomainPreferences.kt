package illyan.butler.domain.model

import kotlinx.serialization.Serializable

@Serializable
class DomainPreferences(
    val userUUID: String? = null,
    val analyticsEnabled: Boolean = false,
    val theme: Theme = Theme.System,
) {
    companion object {
        val Default = DomainPreferences()
    }
}