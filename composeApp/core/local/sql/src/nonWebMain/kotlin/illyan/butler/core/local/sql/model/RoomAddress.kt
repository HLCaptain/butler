package illyan.butler.core.local.sql.model

import kotlinx.serialization.Serializable

@Serializable
data class RoomAddress(
    val street: String,
    val city: String,
    val state: String,
    val zip: String
)
