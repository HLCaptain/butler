package illyan.butler.data.network.datasource

import illyan.butler.data.network.model.MessageDto

interface MessageNetworkDataSource {
    suspend fun fetch(uuid: String): MessageDto

    /**
     * Fetch messages from a chat.
     * TODO: make this paginated.
     * @return messages in the chat.
     */
    suspend fun fetchByChat(chatUUID: String): List<MessageDto>

    /**
     * Update a message.
     * @return updated message.
     */
    suspend fun upsert(message: MessageDto): MessageDto

    /**
     * Delete a message.
     * @return true if the message is deleted.
     */
    suspend fun delete(uuid: String): Boolean
}