package illyan.butler.data.network.model.chat

import kotlinx.serialization.Serializable

@Serializable
data class ChatDto(
    val id: String?,
    val created: Long?,
    val name: String?,
    val members: List<String>,
    val lastFewMessages: List<MessageDto> = emptyList()
)
