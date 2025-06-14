package illyan.butler.data.chat

import illyan.butler.domain.model.Chat
import illyan.butler.shared.model.chat.Source
import kotlinx.coroutines.flow.Flow
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
interface ChatRepository {
    fun getChatFlow(chatId: Uuid, source: Source): Flow<Chat?>
    fun getChatFlowBySource(source: Source): Flow<List<Chat>?>
    suspend fun upsert(chat: Chat): Uuid
    suspend fun deleteChat(chat: Chat)
}
