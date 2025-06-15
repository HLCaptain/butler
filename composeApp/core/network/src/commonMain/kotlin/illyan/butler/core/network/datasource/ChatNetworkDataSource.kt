package illyan.butler.core.network.datasource

import illyan.butler.domain.model.Chat
import illyan.butler.shared.model.chat.Source
import kotlinx.coroutines.flow.Flow
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
interface ChatNetworkDataSource {
    fun fetchNewChats(source: Source.Server): Flow<List<Chat>>

    /**
     * Fetch all chats the user is a member of.
     */
    suspend fun fetch(source: Source.Server): List<Chat>

    fun fetchByChatId(source: Source.Server, chatId: Uuid): Flow<Chat>

    fun fetchByUserId(source: Source.Server): Flow<List<Chat>>

    /**
     * Update a chat (i.e. add/remove members)
     */
    suspend fun upsert(chat: Chat): Chat

    suspend fun create(chat: Chat): Chat

    /**
     * Try to delete a chat from the user's perspective. Basically, the user leaves the chat.
     * @return true if chat is deleted.
     */
    suspend fun delete(chat: Chat): Boolean
}
