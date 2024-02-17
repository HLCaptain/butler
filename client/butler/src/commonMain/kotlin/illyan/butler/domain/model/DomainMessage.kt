package illyan.butler.domain.model

data class DomainMessage(
    val uuid: String,
    val senderUUID: String,
    val role: String,
    val message: String,
    val timestamp: Long,
    val chatUUID: String
) {
    companion object {
        const val USER_ROLE = "user"
    }
}
