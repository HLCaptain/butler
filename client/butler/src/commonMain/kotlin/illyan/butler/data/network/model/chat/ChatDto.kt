package illyan.butler.data.network.model.chat

import kotlinx.serialization.Serializable

@Serializable
data class ChatDto(
    val id: String? = null,
    val created: Long? = null,
    val name: String? = null,
    val members: List<String> = emptyList(),
    val lastFewMessages: List<MessageDto> = emptyList()
)
