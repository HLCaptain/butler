package illyan.butler.data.network.model.chat

import kotlinx.serialization.Serializable

@Serializable
data class MessageDto(
    val id: String?,
    val senderId: String,
    val role: String,
    val message: String,
    val timestamp: Long,
    val chatId: String
)
