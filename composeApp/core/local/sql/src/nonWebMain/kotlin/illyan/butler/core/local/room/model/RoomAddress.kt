package illyan.butler.core.local.room.model

import kotlinx.serialization.Serializable

@Serializable
data class RoomAddress(
    val street: String,
    val city: String,
    val state: String,
    val zip: String
)
