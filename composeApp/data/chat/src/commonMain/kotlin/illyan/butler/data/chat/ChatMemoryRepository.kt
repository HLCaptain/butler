package illyan.butler.data.chat

import illyan.butler.data.settings.AppRepository
import illyan.butler.domain.model.DomainChat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import org.koin.core.annotation.Single

@Single
class ChatMemoryRepository(
    private val appRepository: AppRepository
) : ChatRepository {
    private val chats = mutableMapOf<String, DomainChat>()
    private val userChats = mutableMapOf<String, List<DomainChat>>()

    private val chatStateFlows = mutableMapOf<String, MutableStateFlow<Pair<DomainChat?, Boolean>>>()
    override fun getChatFlow(chatId: String, deviceOnly: Boolean): StateFlow<Pair<DomainChat?, Boolean>> {
        return chatStateFlows.getOrPut(chatId) {
            MutableStateFlow(chats[chatId] to false)
        }
    }

    private val userChatStateFlows = mutableMapOf<String, MutableStateFlow<Pair<List<DomainChat>?, Boolean>>>()
    override suspend fun deleteChat(chatId: String) {
        val userId = chats[chatId]?.ownerId!!
        chats.remove(chatId)
        chatStateFlows[chatId]?.update { null to false }
        chatStateFlows.remove(chatId)

        userChats[userId] = userChats[userId]?.filterNot { it.id == chatId } ?: emptyList()
        userChatStateFlows[userId]?.update { userChats[userId] to false }
    }

    override fun getUserChatsFlow(userId: String, deviceOnly: Boolean): StateFlow<Pair<List<DomainChat>?, Boolean>> {
        return userChatStateFlows.getOrPut(userId) {
            MutableStateFlow(userChats[userId] to false)
        }
    }

    override suspend fun upsert(chat: DomainChat, deviceOnly: Boolean): String {
        val newChat = if (chat.id == null) {
            chat.copy(id = ((chats.values.maxOfOrNull { it.id?.toInt() ?: 0 } ?: 0) + 1).toString())
        } else chat

        chats[newChat.id!!] = newChat
        chatStateFlows[newChat.id]?.update { newChat to false }
        val userId = if (deviceOnly) {
            appRepository.appSettings.first()!!.clientId
        } else {
            appRepository.currentSignedInUserId.first()!!
        }
        userChats[userId] = userChats[userId]?.plus(newChat) ?: listOf(newChat)
        userChatStateFlows[userId]?.update { userChats[userId] to false }

        return newChat.id!!
    }

    override suspend fun deleteAllChats(userId: String) {
        userChats.remove(userId)
        userChatStateFlows[userId]?.update { null to false }
    }
}
