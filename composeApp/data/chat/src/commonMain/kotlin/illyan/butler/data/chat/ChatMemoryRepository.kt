package illyan.butler.data.chat

import illyan.butler.data.settings.AppRepository
import illyan.butler.domain.model.Chat
import illyan.butler.shared.model.chat.Source
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import org.koin.core.annotation.Single
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
@Single
class ChatMemoryRepository(
    private val appRepository: AppRepository
) : ChatRepository {
    private val chats = mutableMapOf<Uuid, Chat>()

    private val chatStateFlows = mutableMapOf<Uuid, MutableStateFlow<Chat?>>()
    override fun getChatFlow(chatId: Uuid, source: Source): Flow<Chat?> {
        return chatStateFlows.getOrPut(chatId) {
            MutableStateFlow(chats[chatId])
        }
    }

    override suspend fun deleteChat(chatId: Uuid) {
        chats.remove(chatId)
        chatStateFlows[chatId]?.update { null }
        chatStateFlows.remove(chatId)
    }

    override suspend fun upsert(chat: Chat): Uuid {
        chats[chat.id] = chat
        chatStateFlows[chat.id]?.update { chat }

        return chat.id
    }
}
