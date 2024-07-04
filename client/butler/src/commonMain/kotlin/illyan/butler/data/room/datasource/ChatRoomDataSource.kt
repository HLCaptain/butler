package illyan.butler.data.room.datasource

import illyan.butler.data.local.datasource.ChatLocalDataSource
import illyan.butler.data.mapping.toDomainModel
import illyan.butler.data.mapping.toRoomModel
import illyan.butler.data.room.dao.ChatDao
import illyan.butler.data.room.dao.ChatMemberDao
import illyan.butler.data.room.model.RoomChatMember
import illyan.butler.domain.model.DomainChat
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Single

@Single
class ChatRoomDataSource(
    private val chatDao: ChatDao,
    private val chatMemberDao: ChatMemberDao
) : ChatLocalDataSource {
    override fun getChat(key: String): Flow<DomainChat?> {
        return chatDao.getChatById(key).map { it.toDomainModel() }
    }

    override suspend fun upsertChat(chat: DomainChat) {
        chatDao.upsertChat(chat.toRoomModel())
        chatMemberDao.upsertChatMembers(chat.members.map { RoomChatMember(chat.id!!, it) })
    }

    override suspend fun deleteChatById(chatId: String) {
        chatDao.deleteChatById(chatId)
        chatMemberDao.deleteChatMembersByChatId(chatId)
    }

    override suspend fun deleteChatByUserId(userId: String) {
        val chatIds = chatMemberDao.getUserChatIds(userId).first()
        chatDao.deleteChatsByUserId(userId)
        chatMemberDao.deleteChatMembersByChatIds(chatIds)
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
        chatMemberDao.upsertChatMembers(chats.flatMap { chat ->
            chat.members.map { RoomChatMember(chat.id!!, it) }
        })
        chatDao.upsertChats(chats.map { it.toRoomModel() })
    }
}
