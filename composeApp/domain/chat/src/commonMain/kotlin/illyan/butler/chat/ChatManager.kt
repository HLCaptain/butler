package illyan.butler.chat

import illyan.butler.auth.AuthManager
import illyan.butler.data.chat.ChatRepository
import illyan.butler.data.message.MessageRepository
import illyan.butler.data.resource.ResourceRepository
import illyan.butler.domain.model.DomainChat
import illyan.butler.domain.model.DomainMessage
import illyan.butler.domain.model.DomainResource
import io.github.aakira.napier.Napier
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNot
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.readByteArray
import org.koin.core.annotation.Single

@OptIn(ExperimentalCoroutinesApi::class)
@Single
class ChatManager(
    private val authManager: AuthManager,
    private val chatRepository: ChatRepository,
    private val messageRepository: MessageRepository,
    private val resourceRepository: ResourceRepository,
) {
    val userChats = authManager.signedInUserId.flatMapLatest { loadChats(it, false) }
    val deviceChats = authManager.clientId.flatMapLatest { loadChats(it, true) }
    private val userMessages = authManager.signedInUserId.flatMapLatest { loadMessages(it, false) }
    private val deviceMessages = authManager.clientId.flatMapLatest { loadMessages(it, true) }

    private val userResources = userMessages.flatMapLatest { messages ->
        // map to Flow<Map<String?, List<DomainResource?>>>
        combine(messages.filter { it.resourceIds.isNotEmpty() }.groupBy { it.id }.mapValues { (_, messages) ->
            combine(messages.map { message ->
                combine(message.resourceIds.map { loadResource(it) }) { it.toList() }
            }) { flows -> flows.toList().flatten().distinctBy { it?.id } }
        }.map { (key, value) -> value.map { key to it } }) { resources ->
            resources.toList().toMap()
        }
    }

    private val deviceResources = deviceMessages.flatMapLatest { messages ->
        // map to Flow<Map<String?, List<DomainResource?>>>
        combine(messages.filter { it.resourceIds.isNotEmpty() }.groupBy { it.id }.mapValues { (_, messages) ->
            combine(messages.map { message ->
                combine(message.resourceIds.map { loadResource(it) }) { it.toList() }
            }) { flows -> flows.toList().flatten().distinctBy { it?.id } }
        }.map { (key, value) -> value.map { key to it } }) { resources ->
            resources.toList().toMap()
        }
    }

    private fun loadChats(userId: String?, deviceOnly: Boolean): Flow<List<DomainChat>> {
        if (userId == null) {
            Napier.v { "User not signed in, reseting chats" }
            return flowOf(emptyList())
        }
        Napier.v { "Loading chats for user $userId" }
        return chatRepository.getUserChatsFlow(userId, deviceOnly).filterNot { it.second }.map { it.first ?: emptyList() }
    }

    private fun loadMessages(userId: String?, deviceOnly: Boolean): Flow<List<DomainMessage>> {
        if (userId == null) {
            Napier.v { "User not signed in, reseting messages" }
            return flowOf(emptyList())
        }
        Napier.v { "Loading messages for user $userId" }
        return messageRepository.getUserMessagesFlow(userId, deviceOnly).filterNot { it.second }.map { it.first ?: emptyList() }
    }

    private fun loadResource(resourceId: String): Flow<DomainResource?> {
        return resourceRepository.getResourceFlow(resourceId).filterNot { it.second }.map { it.first }
    }

    fun getChatFlow(chatId: String) = combine(userChats, deviceChats) { user, device -> (user + device).firstOrNull { it.id == chatId } }
    fun getMessagesByChatFlow(chatId: String) = combine(userMessages, deviceMessages) { user, device -> (user + device).filter { it.chatId == chatId } }
    fun getResourcesByMessageFlow(messageId: String) = combine(userResources, deviceResources) { user, device -> (user + device)[messageId] }

    suspend fun startNewChat(
        modelId: String,
        endpoint: String,
        senderId: String
    ): String {
        return chatRepository.upsert(
            DomainChat(
                ownerId = senderId,
                chatCompletionModel = endpoint to modelId
            ),
            deviceOnly = authManager.clientId.first() == senderId
        )
    }

    suspend fun nameChat(
        chatId: String,
        name: String,
    ) {
        userChats.first().firstOrNull { it.id == chatId }?.let { chat ->
            chatRepository.upsert(chat.copy(name = name), authManager.clientId.first() == chat.ownerId)
        }
    }

    suspend fun sendMessage(
        chatId: String,
        senderId: String,
        message: String? = null,
        resourceIds: List<String> = emptyList(),
    ) {
        messageRepository.upsert(
            DomainMessage(
                chatId = chatId,
                senderId = senderId,
                message = message,
                resourceIds = resourceIds
            ),
            deviceOnly = authManager.clientId.first() == senderId
        )
    }

    suspend fun sendAudioMessage(chatId: String, audioId: String, senderId: String) {
        sendMessage(chatId, senderId, resourceIds = listOf(audioId))
    }

    suspend fun sendImageMessage(chatId: String, imagePath: String, senderId: String) {
        val path = Path(imagePath)
        val imageContent = SystemFileSystem.source(path).buffered().readByteArray()
        val imageId = resourceRepository.upsert(
            DomainResource(
                type = "image/${path.name.substringAfterLast('.').lowercase()}",
                data = imageContent
            )
        )
        sendMessage(chatId, senderId, resourceIds = listOf(imageId))
    }

    suspend fun deleteChat(chatId: String) {
        chatRepository.deleteChat(chatId)
    }
}
