package illyan.butler.data.room.model

import androidx.room.Entity
import illyan.butler.domain.model.DomainPreferences
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
