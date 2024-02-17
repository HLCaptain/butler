package illyan.butler.data.network.model

import kotlinx.serialization.Serializable

@Serializable
data class MessageDto(
    val uuid: String,
    val senderUUID: String,
    val role: String,
    val message: String,
    val timestamp: Long,
    val chatUUID: String
)
