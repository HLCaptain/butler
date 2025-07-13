package illyan.butler.core.local.sql.model

import illyan.butler.domain.model.Theming
import kotlinx.serialization.Serializable

@Serializable
data class RoomPreferences(
    val userId: String? = null,
    val analyticsEnabled: Boolean = false,
    val theming: Theming = Theming.Default
)
