package illyan.butler.core.local.room.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
@Entity(tableName = "users")
data class RoomUser(
    @PrimaryKey
    val id: Uuid,
    val endpoint: String, // e.g. "https://api.example.com/"
    val email: String,
    val username: String?,
    val displayName: String?,
    val phone: String?,
    val fullName: String?,
    val photoUrl: String?,
    val address: RoomAddress?,
    val accessToken: RoomToken?,
    val refreshToken: RoomToken?
)
