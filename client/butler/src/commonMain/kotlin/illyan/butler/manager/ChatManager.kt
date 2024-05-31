package illyan.butler.manager

import illyan.butler.di.KoinNames
import illyan.butler.domain.model.DomainChat
import illyan.butler.domain.model.DomainMessage
import illyan.butler.domain.model.DomainResource
import illyan.butler.repository.chat.ChatRepository
import illyan.butler.repository.message.MessageRepository
import illyan.butler.repository.resource.ResourceRepository
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNot
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.readByteArray
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single

@OptIn(ExperimentalCoroutinesApi::class)
@Single
class ChatManager(
    private val authManager: AuthManager,
    private val chatRepository: ChatRepository,
    private val messageRepository: MessageRepository,
    private val resourceRepository: ResourceRepository,
    @Named(KoinNames.CoroutineScopeIO) private val coroutineScopeIO: CoroutineScope
) {
    private val _userChats = MutableStateFlow<List<DomainChat>>(emptyList())
    val userChats = _userChats.asStateFlow()

    private val _userMessages = MutableStateFlow<List<DomainMessage>>(emptyList())
    private val userMessages = _userMessages.asStateFlow()

    private val messageResources = userMessages.flatMapLatest { messages ->
        // map to Flow<Map<String?, List<DomainResource?>>>
        combine(messages.filter { it.resourceIds.isNotEmpty() }.groupBy { it.id }.mapValues { (_, messages) ->
            combine(messages.map { message ->
                combine(message.resourceIds.map { loadResource(it) }) { it.toList() }
            }) { flows -> flows.toList().flatten().distinctBy { it?.id } }
        }.map { (key, value) -> value.map { key to it } }) { resources ->
            resources.toList().toMap()
        }
    }.stateIn(
        coroutineScopeIO,
        SharingStarted.Eagerly,
        emptyMap()
    )

    init {
        coroutineScopeIO.launch {
            authManager.signedInUserId.flatMapLatest {
                _userChats.update { emptyList() }
                loadChats(it)
            }.collectLatest { newChats ->
                _userChats.update { chats -> (chats + newChats).distinctBy { it.id } }
            }
        }
        coroutineScopeIO.launch {
            authManager.signedInUserId.flatMapLatest {
                _userMessages.update { emptyList() }
                loadMessages(it)
            }.collectLatest { newMessages ->
                _userMessages.update { messages -> (messages + newMessages).distinctBy { it.id } }
            }
        }
    }

    private fun loadChats(userId: String? = authManager.signedInUserId.value): Flow<List<DomainChat>> {
        if (userId == null) {
            Napier.v { "User not signed in, reseting chats" }
            return flowOf(emptyList())
        }
        Napier.v { "Loading chats for user $userId" }
        return chatRepository.getUserChatsFlow(userId).filterNot { it.second }.map { it.first ?: emptyList() }
    }

    private fun loadMessages(userId: String? = authManager.signedInUserId.value): Flow<List<DomainMessage>> {
        if (userId == null) {
            Napier.v { "User not signed in, reseting messages" }
            return flowOf(emptyList())
        }
        Napier.v { "Loading messages for user $userId" }
        return messageRepository.getUserMessagesFlow(userId).filterNot { it.second }.map { it.first ?: emptyList() }
    }

    private fun loadResource(resourceId: String): Flow<DomainResource?> {
        return resourceRepository.getResourceFlow(resourceId).filterNot { it.second }.map { it.first }
    }

    fun getChatFlow(chatId: String) = userChats.map { chats -> chats.firstOrNull { it.id == chatId } }
    fun getMessagesByChatFlow(chatId: String) = userMessages.map { messages -> messages.filter { it.chatId == chatId } }
    fun getResourcesByMessageFlow(messageId: String) = messageResources.map { resources -> resources[messageId] }


    suspend fun startNewChat(modelId: String, endpoint: String? = null): String {
        return authManager.signedInUserId.first()?.let { userId ->
            chatRepository.upsert(
                DomainChat(
                    members = listOf(userId, modelId),
                    aiEndpoints = endpoint?.let { mapOf(modelId to it) } ?: emptyMap()
                )
            )
        } ?: throw IllegalArgumentException("User not signed in")
    }

    suspend fun nameChat(chatId: String, name: String) {
        userChats.first().firstOrNull { it.id == chatId }?.let { chat ->
            chatRepository.upsert(chat.copy(name = name))
        }
    }

    suspend fun sendMessage(
        chatId: String,
        message: String? = null,
        resourceIds: List<String> = emptyList()
    ) {
        authManager.signedInUserId.value?.let { userId ->
            messageRepository.upsert(
                DomainMessage(
                    chatId = chatId,
                    senderId = userId,
                    message = message,
                    resourceIds = resourceIds
                )
            )
        }
    }

    suspend fun sendAudioMessage(chatId: String, audioId: String) {
        sendMessage(chatId, resourceIds = listOf(audioId))
    }

    suspend fun sendImageMessage(chatId: String, imagePath: String) {
        val path = Path(imagePath)
        val imageContent = SystemFileSystem.source(path).buffered().readByteArray()
        val imageId = resourceRepository.upsert(
            DomainResource(
                type = "image/${path.name.substringAfterLast('.').lowercase()}",
                data = imageContent
            )
        )
        sendMessage(chatId, resourceIds = listOf(imageId))
    }

    suspend fun deleteChat(chatId: String) {
        chatRepository.deleteChat(chatId)
    }
}