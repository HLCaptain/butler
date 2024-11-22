package illyan.butler.core.local.room.model

import kotlinx.serialization.Serializable

@Serializable
data class RoomToken(
    val token: String,
    val tokenExpirationMillis: Long
)
