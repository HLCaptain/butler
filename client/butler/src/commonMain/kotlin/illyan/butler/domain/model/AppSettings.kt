package illyan.butler.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class AppSettings(
    val clientId: String? = null,
    val preferences: DomainPreferences = DomainPreferences.Default,
) {
    companion object {
        val default = AppSettings()
    }
}