package illyan.butler.core.network.datasource

import illyan.butler.domain.model.DomainChat
import kotlinx.coroutines.flow.Flow

interface ChatNetworkDataSource {
    fun fetchNewChats(): Flow<List<DomainChat>>

    /**
     * Fetch chats the user is a member of.
     */
    suspend fun fetchPaginated(limit: Int, timestamp: Long): List<DomainChat>

    /**
     * Fetch all chats the user is a member of.
     */
    suspend fun fetch(): List<DomainChat>

    fun fetchByChatId(chatId: String): Flow<DomainChat>

    fun fetchByUserId(userId: String): Flow<List<DomainChat>>

    /**
     * Fetch chats the user is a member of with a specific chatbot.
     * TODO: make this paginated.
     */
    suspend fun fetchByModel(modelId: String): List<DomainChat>

    /**
     * Update a chat (i.e. add/remove members)
     */
    suspend fun upsert(chat: DomainChat): DomainChat

    /**
     * Try to delete a chat from the user's perspective. Basically, the user leaves the chat.
     * @return true if chat is deleted.
     */
    suspend fun delete(id: String): Boolean
}