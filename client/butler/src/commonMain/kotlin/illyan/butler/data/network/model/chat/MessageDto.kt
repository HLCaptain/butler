package illyan.butler.data.network.model.chat

import kotlinx.serialization.Serializable

@Serializable
data class MessageDto(
    val id: String? = null,
    val senderId: String,
    /**
     * For a message there always can be text based content.
     */
    val message: String? = null,
    /**
     * To include pictures, media, etc. for a message.
     */
    val contentUrls: List<String> = emptyList(),
    val time: Long? = null, // Unix timestamp
    val chatId: String
)