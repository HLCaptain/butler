package illyan.butler.model

import kotlinx.serialization.Serializable

@Serializable
data class DomainPreferences(
    val userId: String? = null,
    val analyticsEnabled: Boolean = false,
    val dynamicColorEnabled: Boolean = true,
    val theme: Theme = Theme.System
    // TODO: Add shouldSync option and lastUpdate timestamp
) {
    companion object {
        val Default = DomainPreferences()
    }
}