package illyan.butler.core.local.room.datasource

import illyan.butler.core.local.datasource.ChatLocalDataSource
import illyan.butler.core.local.room.dao.ChatDao
import illyan.butler.core.local.room.mapping.toDomainModel
import illyan.butler.core.local.room.mapping.toRoomModel
import illyan.butler.domain.model.Chat
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Single
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
@Single
class ChatRoomDataSource(
    private val chatDao: ChatDao
) : ChatLocalDataSource {
    override fun getChat(id: Uuid): Flow<Chat?> {
        return chatDao.getChatById(id).map { it?.toDomainModel() }
    }

    override suspend fun upsertChat(chat: Chat) {
        chatDao.upsertChat(chat.toRoomModel())
    }

    override suspend fun replaceChat(oldChatId: String, newChat: Chat) {
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

    override fun getChatsByUser(userId: String): Flow<List<Chat>?> {
        return chatDao.getChatsByUser(userId).map { chats ->
            chats.map { it.toDomainModel() }
        }
    }

    override suspend fun upsertChats(chats: List<Chat>) {
        chatDao.upsertChats(chats.map { it.toRoomModel() })
    }
}
