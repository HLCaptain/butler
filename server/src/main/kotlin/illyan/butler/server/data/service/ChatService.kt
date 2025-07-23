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
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

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
@OptIn(ExperimentalUuidApi::class)
@Single
class ChatService(
    private val chatDatabase: ChatDatabase,
    private val messageDatabase: MessageDatabase,
    private val resourceDatabase: ResourceDatabase
) : ChatDataSource, MessageDataSource, ResourceDataSource {
    override suspend fun getChat(userId: Uuid, chatId: Uuid): ChatDto {
        return chatDatabase.getChat(userId, chatId)
    }

    override suspend fun createChat(userId: Uuid, chat: ChatDto): ChatDto {
        return chatDatabase.createChat(userId, chat)
    }

    override suspend fun editChat(userId: Uuid, chat: ChatDto): ChatDto {
        return chatDatabase.editChat(userId, chat)
    }

    override suspend fun deleteChat(userId: Uuid, chatId: Uuid): Boolean {
        return chatDatabase.deleteChat(userId, chatId)
    }

    override suspend fun getChats(userId: Uuid): List<ChatDto> {
        return chatDatabase.getChats(userId)
    }

    override suspend fun getChats(userId: Uuid, limit: Int, offset: Int): List<ChatDto> {
        return chatDatabase.getChats(userId, limit, offset)
    }

    override suspend fun getChats(userId: Uuid, fromDate: Long, toDate: Long): List<ChatDto> {
        return chatDatabase.getChats(userId, fromDate, toDate)
    }

    override suspend fun getPreviousChats(userId: Uuid, limit: Int, timestamp: Long): List<ChatDto> {
        return chatDatabase.getPreviousChats(userId, limit, timestamp)
    }

    override suspend fun getPreviousChats(userId: Uuid, limit: Int, offset: Int): List<ChatDto> {
        return chatDatabase.getPreviousChats(userId, limit, offset)
    }

    override fun getChangedChatsAffectingUser(userId: Uuid): Flow<List<ChatDto>> {
        return chatDatabase.getChangedChatsAffectingUser(userId)
    }

    override fun getChangesFromChat(userId: Uuid, chatId: Uuid): Flow<ChatDto> {
        return chatDatabase.getChangesFromChat(userId, chatId)
    }

    override suspend fun sendMessage(userId: Uuid, message: MessageDto): MessageDto {
        return messageDatabase.sendMessage(userId, message)
    }

    override suspend fun editMessage(userId: Uuid, message: MessageDto): MessageDto {
        return messageDatabase.editMessage(userId, message)
    }

    override suspend fun deleteMessage(userId: Uuid, chatId: Uuid, messageId: Uuid): Boolean {
        return messageDatabase.deleteMessage(userId, chatId, messageId)
    }

    override suspend fun getPreviousMessages(
        userId: Uuid,
        chatId: Uuid,
        limit: Int,
        timestamp: Long
    ): List<MessageDto> {
        return messageDatabase.getPreviousMessages(userId, chatId, limit, timestamp)
    }

    override suspend fun getMessages(userId: Uuid, chatId: Uuid, limit: Int, offset: Int): List<MessageDto> {
        return messageDatabase.getMessages(userId, chatId, limit, offset)
    }

    override suspend fun getMessages(userId: Uuid, chatId: Uuid): List<MessageDto> {
        return messageDatabase.getMessages(userId, chatId)
    }

    override suspend fun getMessages(userId: Uuid): List<MessageDto> {
        return messageDatabase.getMessages(userId)
    }

    override fun getChangedMessagesByUser(userId: Uuid): Flow<List<MessageDto>> {
        return messageDatabase.getChangedMessagesAffectingUser(userId)
    }

    override fun getChangedMessagesByChat(userId: Uuid, chatId: Uuid): Flow<List<MessageDto>> {
        return messageDatabase.getChangedMessagesAffectingChat(userId, chatId)
    }

    override suspend fun createResource(resource: ResourceDto): ResourceDto {
        return resourceDatabase.createResource(resource)
    }

    override suspend fun getResource(userId: Uuid, resourceId: Uuid): ResourceDto {
        return resourceDatabase.getResource(userId, resourceId)
    }

    override suspend fun deleteResource(userId: Uuid, resourceId: Uuid): Boolean {
        return resourceDatabase.deleteResource(userId, resourceId)
    }

    override suspend fun getResources(userId: Uuid): List<ResourceDto> {
        return resourceDatabase.getResources(userId)
    }
}