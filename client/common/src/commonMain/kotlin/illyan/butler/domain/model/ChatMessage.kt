package illyan.butler.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class ChatMessage(
    val uuid: String,
    val senderUUID: String,
    val role: String,
    val message: String,
    val timestamp: Long,
) {
    companion object {
        const val UserRole = "user"
        const val BotRole = "bot"
        const val SystemRole = "system"
    }
}
