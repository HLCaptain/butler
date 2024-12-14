package illyan.butler.core.local.room.model

import androidx.room.Entity
import kotlinx.serialization.Serializable

@Entity(tableName = "app_settings", primaryKeys = ["clientId"])
@Serializable
data class RoomAppSettings(
    val clientId: String,
    val preferences: RoomPreferences,
    val firstSignInHappenedYet: Boolean,
    val hostUrl: String,
)
