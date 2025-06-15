package illyan.butler.core.local.datasource

import illyan.butler.domain.model.Chat
import illyan.butler.shared.model.chat.Source
import kotlinx.coroutines.flow.Flow
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
interface ChatLocalDataSource {
    fun getChat(chatId: Uuid): Flow<Chat?>
    fun getChatsBySource(source: Source): Flow<List<Chat>>
    suspend fun upsertChat(chat: Chat)
    suspend fun replaceChat(oldChatId: Uuid, newChat: Chat)
    suspend fun deleteChatById(chatId: Uuid)
    suspend fun deleteAllChats()
    suspend fun upsertChats(chats: List<Chat>)
}
