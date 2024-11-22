package illyan.butler.domain.model

import illyan.butler.core.utils.randomUUID
import kotlinx.serialization.Serializable

@Serializable
data class AppSettings(
    val clientId: String = randomUUID(),
    val preferences: DomainPreferences = DomainPreferences.Default,
    val hostUrl: String = "",
) {
    companion object {
        val Default = AppSettings()
    }
}