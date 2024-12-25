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

    sealed class Delete : MessageKey() {
        data class ByMessageId(val messageId: String) : Delete()
        data class ByChatId(val chatId: String) : Delete()
    }
}