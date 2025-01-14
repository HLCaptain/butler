package illyan.butler.core.local.room.datasource

import illyan.butler.core.local.datasource.ChatLocalDataSource
import illyan.butler.core.local.room.dao.ChatDao
import illyan.butler.core.local.room.mapping.toDomainModel
import illyan.butler.core.local.room.mapping.toRoomModel
import illyan.butler.domain.model.DomainChat
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Single

@Single
class ChatRoomDataSource(
    private val chatDao: ChatDao
) : ChatLocalDataSource {
    override fun getChat(key: String): Flow<DomainChat?> {
        return chatDao.getChatById(key).map { it?.toDomainModel() }
    }

    override suspend fun upsertChat(chat: DomainChat) {
        chatDao.upsertChat(chat.toRoomModel())
    }

    override suspend fun replaceChat(oldChatId: String, newChat: DomainChat) {
        if (oldChatId == newChat.id) {
            upsertChat(newChat)
        } else {
            chatDao.replaceChat(oldChatId, newChat.toRoomModel())
        }
    }

    override suspend fun deleteChatById(chatId: String) {
        chatDao.deleteChatById(chatId)
    }

    override suspend fun deleteChatsForUser(userId: String) {
        chatDao.deleteChatsByUserId(userId)
    }

    override suspend fun deleteAllChats() {
        chatDao.deleteAllChats()
    }

    override fun getChatsByUser(userId: String): Flow<List<DomainChat>?> {
        return chatDao.getChatsByUser(userId).map { chats ->
            chats.map { it.toDomainModel() }
        }
    }

    override suspend fun upsertChats(chats: List<DomainChat>) {
        chatDao.upsertChats(chats.map { it.toRoomModel() })
    }
}
