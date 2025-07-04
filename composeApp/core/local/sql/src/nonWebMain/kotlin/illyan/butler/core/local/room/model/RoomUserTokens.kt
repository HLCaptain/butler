package illyan.butler.core.local.room.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_tokens")
data class RoomUserTokens(
    @PrimaryKey
    val userId: String,
    val accessToken: RoomToken?,
    val refreshToken: RoomToken?,
)
