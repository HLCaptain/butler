package illyan.butler.core.local.room.datasource

import illyan.butler.core.local.datasource.ChatLocalDataSource
import illyan.butler.core.local.room.dao.ChatDao
import illyan.butler.core.local.room.mapping.toDomainModel
import illyan.butler.core.local.room.mapping.toRoomModel
import illyan.butler.domain.model.Chat
import illyan.butler.shared.model.chat.Source
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
    override fun getChat(chatId: Uuid): Flow<Chat?> {
        return chatDao.getChatById(chatId.toString()).map { it?.toDomainModel() }
    }

    override fun getChatsBySource(source: Source): Flow<List<Chat>> {
        return chatDao.getChatsBySource(source).map { chats ->
            chats.map { it.toDomainModel() }
        }
    }

    override suspend fun upsertChat(chat: Chat) {
        chatDao.upsertChat(chat.toRoomModel())
    }

    override suspend fun deleteAllChats() {
        chatDao.deleteAllChats()
    }

    override suspend fun replaceChat(
        oldChatId: Uuid,
        newChat: Chat
    ) {
        if (oldChatId == newChat.id) {
            upsertChat(newChat)
        } else {
            chatDao.replaceChat(oldChatId.toString(), newChat.toRoomModel())
        }
    }

    override suspend fun deleteChatById(chatId: Uuid) {
        chatDao.deleteChatById(chatId.toString())
    }

    override suspend fun upsertChats(chats: List<Chat>) {
        chatDao.upsertChats(chats.map { it.toRoomModel() })
    }
}
