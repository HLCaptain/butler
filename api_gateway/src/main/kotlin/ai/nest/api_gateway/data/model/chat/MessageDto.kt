package ai.nest.api_gateway.data.model.chat

import kotlinx.serialization.Serializable

@Serializable
data class MessageDto(
    val id: String? = null,
    val senderId: String? = null,
    val content: String? = null,
    val time: Long? = null
)
