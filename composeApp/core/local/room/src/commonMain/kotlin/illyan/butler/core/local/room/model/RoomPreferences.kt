package illyan.butler.core.local.room.model

import illyan.butler.domain.model.Theme
import kotlinx.serialization.Serializable

@Serializable
data class RoomPreferences(
    val userId: String? = null,
    val analyticsEnabled: Boolean = false,
    val dynamicColorEnabled: Boolean = true,
    val theme: Theme = Theme.System
)