package illyan.butler.data.local.room.model

import androidx.room.Entity
import illyan.butler.model.DomainPreferences
import kotlinx.serialization.Serializable

@Entity(tableName = "app_settings", primaryKeys = ["clientId"])
@Serializable
data class RoomAppSettings(
    val clientId: String,
    val preferences: DomainPreferences,
    val firstSignInHappenedYet: Boolean,
    val isTutorialDone: Boolean,
    val hostUrl: String,
)
