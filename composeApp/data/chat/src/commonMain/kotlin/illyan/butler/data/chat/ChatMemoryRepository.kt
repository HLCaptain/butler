package illyan.butler.data.chat

import illyan.butler.data.settings.AppRepository
import illyan.butler.domain.model.DomainChat
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.datetime.Clock
import org.koin.core.annotation.Single

@Single
class ChatMemoryRepository(
    private val appRepository: AppRepository
) : ChatRepository {
    private val chats = mutableMapOf<String, DomainChat>()
    private val userChats = mutableMapOf<String, List<DomainChat>>()

    private val chatStateFlows = mutableMapOf<String, MutableStateFlow<DomainChat?>>()
    override fun getChatFlow(chatId: String, deviceOnly: Boolean): Flow<DomainChat?> {
        return chatStateFlows.getOrPut(chatId) {
            MutableStateFlow(chats[chatId])
        }
    }

    private val userChatStateFlows = mutableMapOf<String, MutableStateFlow<List<DomainChat>>>()

    override suspend fun deleteChat(chatId: String, deviceOnly: Boolean) {
        val userId = chats[chatId]?.ownerId!!
        chats.remove(chatId)
        chatStateFlows[chatId]?.update { null }
        chatStateFlows.remove(chatId)

        userChats[userId] = userChats[userId]?.filterNot { it.id == chatId } ?: emptyList()
        userChatStateFlows[userId]?.update { userChats[userId]!! }
    }

    override fun getUserChatsFlow(userId: String, deviceOnly: Boolean): StateFlow<List<DomainChat>> {
        return userChatStateFlows.getOrPut(userId) {
            MutableStateFlow(userChats.getOrPut(userId) { emptyList() })
        }
    }

    override suspend fun upsert(chat: DomainChat, deviceOnly: Boolean): String {
        val newChat = if (chat.id == null) {
            chat.copy(
                id = ((chats.values.maxOfOrNull { it.id?.toInt() ?: 0 } ?: 0) + 1).toString(),
                created = Clock.System.now().toEpochMilliseconds()
            )
        } else chat

        chats[newChat.id!!] = newChat
        chatStateFlows[newChat.id]?.update { newChat }
        val userId = if (deviceOnly) {
            appRepository.appSettings.first()!!.clientId
        } else {
            appRepository.currentSignedInUserId.first()!!
        }
        userChats[userId] = userChats[userId]?.plus(newChat) ?: listOf(newChat)
        userChatStateFlows[userId]?.update { userChats[userId]!! }

        return newChat.id!!
    }

    override suspend fun deleteAllChats(userId: String, deviceOnly: Boolean) {
        userChats.remove(userId)
        userChatStateFlows[userId]?.update { emptyList() }
    }
}
