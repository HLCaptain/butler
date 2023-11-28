package illyan.butler.domain.model

data class DomainChat(
    val uuid: String,
    val name: String? = null,
    val userUUID: String,
    val modelUUID: String,
    val messages: List<ChatMessage>,
)
