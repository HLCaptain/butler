package illyan.butler.data.message

sealed class MessageKey {
    sealed class Read : MessageKey() {
        data class ByMessageId(val messageId: String) : Read()
        data class ByUserId(val userId: String) : Read()
        data class ByChatId(val chatId: String) : Read()
    }

    sealed class Write : MessageKey() {
        data object Create : Write()
        data object Upsert : Write()
        data object DeviceOnly : Write()
    }

    data class Delete(
        val chatId: String, val messageId: String, val deviceOnly: Boolean
    ) : MessageKey()
}