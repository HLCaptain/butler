package illyan.butler.core.local.sql.model

import kotlinx.serialization.Serializable

@Serializable
data class RoomToken(
    val token: String,
    val tokenExpirationMillis: Long
)
