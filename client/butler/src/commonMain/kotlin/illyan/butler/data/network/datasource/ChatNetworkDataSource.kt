package illyan.butler.data.network.datasource

import illyan.butler.data.network.model.chat.ChatDto
import kotlinx.coroutines.flow.Flow

interface ChatNetworkDataSource {
    fun fetchNewChats(): Flow<ChatDto>

    /**
     * Fetch chats the user is a member of.
     */
    suspend fun fetchPaginated(limit: Int, timestamp: Long): List<ChatDto>

    /**
     * Fetch all chats the user is a member of.
     */
    suspend fun fetch(): List<ChatDto>

    /**
     * Fetch chats the user is a member of with a specific chatbot.
     * TODO: make this paginated.
     */
    suspend fun fetchByModel(modelUUID: String): List<ChatDto>

    /**
     * Update a chat (i.e. add/remove members)
     */
    suspend fun upsert(chat: ChatDto): ChatDto

    /**
     * Try to delete a chat from the user's perspective. Basically, the user leaves the chat.
     * @return true if chat is deleted.
     */
    suspend fun delete(uuid: String): Boolean
}