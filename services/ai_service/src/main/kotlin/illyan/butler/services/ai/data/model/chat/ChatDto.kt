package illyan.butler.services.ai.data.model.chat

import kotlinx.serialization.Serializable

@Serializable
data class ChatDto(
    val id: String,
    val created: Long?,
    val name: String? = null,
    val members: List<String> = emptyList(),
    val lastFewMessages: List<MessageDto> = emptyList()
)
