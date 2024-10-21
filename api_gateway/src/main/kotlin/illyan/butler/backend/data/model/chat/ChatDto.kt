package illyan.butler.backend.data.model.chat

import kotlinx.serialization.Serializable

@Serializable
data class ChatDto(
    val id: String? = null,
    val created: Long? = null,
    val name: String? = null,
    val members: List<String> = emptyList(),
    val lastFewMessages: List<MessageDto> = emptyList(),
    val aiEndpoints: Map<String, String>, // senderId -> endpoint, if sender is not in the map, it is considered self-hosted
    val summary: String? = null
)
