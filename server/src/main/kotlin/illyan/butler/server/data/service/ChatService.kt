package illyan.butler.server.data.service

import illyan.butler.server.data.datasource.ChatDataSource
import illyan.butler.server.data.datasource.MessageDataSource
import illyan.butler.server.data.datasource.ResourceDataSource
import illyan.butler.server.data.db.ChatDatabase
import illyan.butler.server.data.db.MessageDatabase
import illyan.butler.server.data.db.ResourceDatabase
import illyan.butler.shared.model.chat.ChatDto
import illyan.butler.shared.model.chat.MessageDto
import illyan.butler.shared.model.chat.ResourceDto
import kotlinx.coroutines.flow.Flow
import org.koin.core.annotation.Single

/**
 * Chat service implementation.
 * Pipeline:
 *  1. Check Cache
 *  2. Update/get resource from Database if needed
 *  3. Update Cache if needed
 *  4. Return resource/flow of resource
 * FIXME: fix caching
 * FIXME: check authorization
 */
@Single
class ChatService(
    private val chatDatabase: ChatDatabase,
    private val messageDatabase: MessageDatabase,
    private val resourceDatabase: ResourceDatabase
) : ChatDataSource, MessageDataSource, ResourceDataSource {
    override suspend fun getChat(userId: String, chatId: String): ChatDto {
        return chatDatabase.getChat(userId, chatId)
    }

    override suspend fun createChat(userId: String, chat: ChatDto): ChatDto {
        return chatDatabase.createChat(userId, chat)
    }

    override suspend fun editChat(userId: String, chat: ChatDto) {
        chatDatabase.editChat(userId, chat)
    }

    override suspend fun deleteChat(userId: String, chatId: String): Boolean {
        return chatDatabase.deleteChat(userId, chatId)
    }

    override suspend fun getChats(userId: String): List<ChatDto> {
        return chatDatabase.getChats(userId)
    }

    override suspend fun getChats(userId: String, limit: Int, offset: Int): List<ChatDto> {
        return chatDatabase.getChats(userId, limit, offset)
    }

    override suspend fun getChats(userId: String, fromDate: Long, toDate: Long): List<ChatDto> {
        return chatDatabase.getChats(userId, fromDate, toDate)
    }

    override suspend fun getPreviousChats(userId: String, limit: Int, timestamp: Long): List<ChatDto> {
        return chatDatabase.getPreviousChats(userId, limit, timestamp)
    }

    override suspend fun getPreviousChats(userId: String, limit: Int, offset: Int): List<ChatDto> {
        return chatDatabase.getPreviousChats(userId, limit, offset)
    }

    override fun getChangedChatsAffectingUser(userId: String): Flow<List<ChatDto>> {
        return chatDatabase.getChangedChatsAffectingUser(userId)
    }

    override fun getChangesFromChat(userId: String, chatId: String): Flow<ChatDto> {
        return chatDatabase.getChangesFromChat(userId, chatId)
    }

    override suspend fun sendMessage(userId: String, message: MessageDto): MessageDto {
        return messageDatabase.sendMessage(userId, message)
    }

    override suspend fun editMessage(userId: String, message: MessageDto): MessageDto {
        return messageDatabase.editMessage(userId, message)
    }

    override suspend fun deleteMessage(userId: String, chatId: String, messageId: String): Boolean {
        return messageDatabase.deleteMessage(userId, chatId, messageId)
    }

    override suspend fun getPreviousMessages(
        userId: String,
        chatId: String,
        limit: Int,
        timestamp: Long
    ): List<MessageDto> {
        return messageDatabase.getPreviousMessages(userId, chatId, limit, timestamp)
    }

    override suspend fun getMessages(userId: String, chatId: String, limit: Int, offset: Int): List<MessageDto> {
        return messageDatabase.getMessages(userId, chatId, limit, offset)
    }

    override suspend fun getMessages(userId: String, chatId: String): List<MessageDto> {
        return messageDatabase.getMessages(userId, chatId)
    }

    override suspend fun getMessages(userId: String): List<MessageDto> {
        return messageDatabase.getMessages(userId)
    }

    override fun getChangedMessagesByUser(userId: String): Flow<List<MessageDto>> {
        return messageDatabase.getChangedMessagesAffectingUser(userId)
    }

    override fun getChangedMessagesByChat(userId: String, chatId: String): Flow<List<MessageDto>> {
        return messageDatabase.getChangedMessagesAffectingChat(userId, chatId)
    }

    override suspend fun createResource(userId: String, resource: ResourceDto): ResourceDto {
        return resourceDatabase.createResource(userId, resource)
    }

    override suspend fun getResource(userId: String, resourceId: String): ResourceDto {
        return resourceDatabase.getResource(userId, resourceId)
    }

    override suspend fun deleteResource(userId: String, resourceId: String): Boolean {
        return resourceDatabase.deleteResource(userId, resourceId)
    }

    override suspend fun getResources(userId: String): List<ResourceDto> {
        return resourceDatabase.getResources(userId)
    }
}