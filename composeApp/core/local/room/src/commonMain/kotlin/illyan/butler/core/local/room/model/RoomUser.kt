package illyan.butler.core.local.room.model

import androidx.room.Entity

@Entity(tableName = "users", primaryKeys = ["id"])
data class RoomUser(
    val id: String,
    val email: String,
    val username: String,
    val displayName: String?,
    val phone: String?,
    val fullName: String?,
    val photoUrl: String?,
    val address: RoomAddress?,
    val accessToken: RoomToken?,
    val refreshToken: RoomToken?
)
