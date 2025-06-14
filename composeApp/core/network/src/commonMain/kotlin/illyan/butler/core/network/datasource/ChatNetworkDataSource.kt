package illyan.butler.core.network.datasource

import illyan.butler.domain.model.Chat
import illyan.butler.shared.model.chat.Source
import kotlinx.coroutines.flow.Flow
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
interface ChatNetworkDataSource {
    fun fetchNewChats(): Flow<List<Chat>>

    /**
     * Fetch chats the user is a member of.
     */
    suspend fun fetchPaginated(source: Source.Server, limit: Int, timestamp: Long): List<Chat>

    /**
     * Fetch all chats the user is a member of.
     */
    suspend fun fetch(source: Source.Server): List<Chat>

    fun fetchByChatId(source: Source.Server, chatId: Uuid): Flow<Chat>

    fun fetchByUserId(source: Source.Server, userId: Uuid): Flow<List<Chat>>

    /**
     * Fetch chats the user is a member of with a specific chatbot.
     * TODO: make this paginated.
     */
    suspend fun fetchByModel(source: Source.Server, modelId: String): List<Chat>

    /**
     * Update a chat (i.e. add/remove members)
     */
    suspend fun upsert(source: Source.Server, chat: Chat): Chat

    /**
     * Try to delete a chat from the user's perspective. Basically, the user leaves the chat.
     * @return true if chat is deleted.
     */
    suspend fun delete(source: Source.Server, chatId: Uuid): Boolean
}