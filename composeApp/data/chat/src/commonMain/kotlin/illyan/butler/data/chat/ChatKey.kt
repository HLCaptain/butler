package illyan.butler.data.chat

sealed class ChatKey {
    sealed class Read : ChatKey() {
        data class ByChatId(val chatId: String) : Read()
        data class ByUserId(val userId: String) : Read()
    }

    sealed class Write : ChatKey() {
        data object Create : Write()
        data object Upsert : Write()
    }

    sealed class Delete : ChatKey() {
        data class ByChatId(val chatId: String) : Delete()
        data class ByUserId(val userId: String) : Delete()
    }
}