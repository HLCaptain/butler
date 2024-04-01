package illyan.butler.domain.model

data class DomainMessage(
    val id: String? = null,
    val senderUUID: String,
    val role: String,
    val message: String,
    val timestamp: Long,
    val chatId: String
) {
    companion object {
        const val USER_ROLE = "user"
    }
}
