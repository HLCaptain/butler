package illyan.butler.data.chat

import illyan.butler.domain.model.Chat
import illyan.butler.shared.model.chat.Source
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import org.koin.core.annotation.Single
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
@Single
class ChatMemoryRepository() : ChatRepository {
    private val chats = MutableStateFlow(emptySet<Chat>())

    override fun getChatFlow(chatId: Uuid, source: Source): Flow<Chat?> =
        chats.map { chatSet ->
            chatSet.firstOrNull { it.id == chatId && it.source == source }
        }

    override suspend fun upsert(chat: Chat): Uuid {
        chats.update { currentChats ->
            currentChats.filterNot { it.id == chat.id }.toSet() + chat
        }
        return chat.id
    }

    override suspend fun deleteChat(chat: Chat) {
        chats.update { currentChats ->
            currentChats.filterNot { it.id == chat.id }.toSet()
        }
    }

    override fun getChatFlowBySource(source: Source): Flow<List<Chat>?> =
        chats.map { chatSet ->
            chatSet.filter { it.source == source }.takeIf { it.isNotEmpty() }
        }

    override suspend fun create(chat: Chat): Uuid {
        chats.update { currentChats ->
            currentChats + chat
        }
        return chat.id
    }
}
