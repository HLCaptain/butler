package illyan.butler.domain.model

data class DomainMessage(
    val id: String? = null,
    val senderId: String,
    /**
     * For a message there always can be text based content.
     */
    val messageContent: String? = null,
    /**
     * To include pictures, media, etc. for a message.
     */
    val resourceIds: List<String> = emptyList(),
    val time: Long? = null, // Unix timestamp
    val chatId: String
)
