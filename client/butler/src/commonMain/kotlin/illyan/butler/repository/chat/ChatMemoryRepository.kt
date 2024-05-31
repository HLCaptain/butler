package illyan.butler.repository.chat

import illyan.butler.domain.model.DomainChat
import illyan.butler.repository.user.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import org.koin.core.annotation.Single

@Single
class ChatMemoryRepository(
    private val userRepository: UserRepository
) : ChatRepository {
    private val chats = mutableMapOf<String, DomainChat>()
    private val userChats = mutableMapOf<String, List<DomainChat>>()

    private val chatStateFlows = mutableMapOf<String, MutableStateFlow<Pair<DomainChat?, Boolean>>>()
    override fun getChatFlow(chatId: String): StateFlow<Pair<DomainChat?, Boolean>> {
        return chatStateFlows.getOrPut(chatId) {
            MutableStateFlow(chats[chatId] to false)
        }
    }

    private val userChatStateFlows = mutableMapOf<String, MutableStateFlow<Pair<List<DomainChat>?, Boolean>>>()
    override suspend fun deleteChat(chatId: String) {
        chats.remove(chatId)
        chatStateFlows[chatId]?.update { null to false }

        val userId = userRepository.signedInUserId.value!!
        userChats[userId] = userChats[userId]?.filterNot { it.id == chatId } ?: emptyList()
        userChatStateFlows[userId]?.update { userChats[userId] to false }
    }

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
        val userId = userRepository.signedInUserId.value!!
        userChats[userId] = userChats[userId]?.plus(newChat) ?: listOf(newChat)
        userChatStateFlows[userId]?.update { userChats[userId] to false }

        return newChat.id
    }

    override suspend fun deleteAllChats(userId: String) {
        userChats.remove(userId)
        userChatStateFlows[userId]?.update { null to false }
    }
}