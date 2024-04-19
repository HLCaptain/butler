package illyan.butler.repository

import illyan.butler.domain.model.DomainChat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import org.koin.core.annotation.Single

@Single
class ChatMemoryRepository : ChatRepository {
    private val chats = mutableMapOf<String, DomainChat>()
    private val userChats = mutableMapOf<String, List<DomainChat>>()

    private val chatStateFlows = mutableMapOf<String, MutableStateFlow<Pair<DomainChat?, Boolean>>>()
    override fun getChatFlow(chatId: String): StateFlow<Pair<DomainChat?, Boolean>> {
        return chatStateFlows.getOrPut(chatId) {
            MutableStateFlow(chats[chatId] to false)
        }
    }

    private val userChatStateFlows = mutableMapOf<String, MutableStateFlow<Pair<List<DomainChat>?, Boolean>>>()
    override fun getUserChatsFlow(userId: String): StateFlow<Pair<List<DomainChat>?, Boolean>> {
        return userChatStateFlows.getOrPut(userId) {
            MutableStateFlow(userChats[userId] to false)
        }
    }

    override suspend fun upsert(chat: DomainChat): String {
        val newChat = if (chat.id == null) {
            chat.copy(id = (chats.size + 1).toString())
        } else chat

        chats[newChat.id!!] = newChat
        chatStateFlows[newChat.id]?.update { newChat to false }

        return newChat.id
    }
}