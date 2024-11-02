package illyan.butler.model

import illyan.butler.utils.randomUUID
import kotlinx.serialization.Serializable

@Serializable
data class AppSettings(
    val clientId: String = randomUUID(),
    val preferences: DomainPreferences = DomainPreferences.Default,
) {
    companion object {
        val Default = AppSettings()
    }
}