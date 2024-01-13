package ai.nest.api_gateway.data.model.chat

import kotlinx.serialization.Serializable

@Serializable
data class TicketDto(
    val id: String? = null,
    val userId: String,
    val statusChanges: Map<Long, Boolean>? = null, // Opened or closed at time
    val chatId: String? = null // Chat with support agent
)
