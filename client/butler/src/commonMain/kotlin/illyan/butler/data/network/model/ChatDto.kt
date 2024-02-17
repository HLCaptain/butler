package illyan.butler.data.network.model

import kotlinx.serialization.Serializable

@Serializable
data class ChatDto(
    val uuid: String,
    val name: String?,
    val userUUID: String,
    val modelUUID: String
)
