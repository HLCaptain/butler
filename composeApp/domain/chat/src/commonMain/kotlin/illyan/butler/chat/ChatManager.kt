package illyan.butler.chat

import illyan.butler.auth.AuthManager
import illyan.butler.core.network.ktor.http.di.provideOpenAIClient
import illyan.butler.core.network.mapping.toDomainModel
import illyan.butler.core.network.mapping.toNetworkModel
import illyan.butler.data.chat.ChatRepository
import illyan.butler.data.credential.CredentialRepository
import illyan.butler.data.message.MessageRepository
import illyan.butler.data.resource.ResourceRepository
import illyan.butler.di.KoinNames
import illyan.butler.domain.model.DomainChat
import illyan.butler.domain.model.DomainMessage
import illyan.butler.domain.model.DomainResource
import illyan.butler.shared.llm.LlmService
import illyan.butler.shared.llm.mapToModelsAndProviders
import illyan.butler.shared.llm.mapToProvidedModels
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.readByteArray
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalCoroutinesApi::class)
@Single
class ChatManager(
    @Named(KoinNames.CoroutineScopeIO) private val coroutineScopeIO: CoroutineScope,
    private val authManager: AuthManager,
    private val chatRepository: ChatRepository,
    private val messageRepository: MessageRepository,
    private val resourceRepository: ResourceRepository,
    private val credentialRepository: CredentialRepository
) {
    val userChats = authManager.signedInUserId.flatMapLatest { loadChats(it, false) }
    val deviceChats = authManager.clientId.flatMapLatest { loadChats(it, true) }
    private val userMessages = authManager.signedInUserId.flatMapLatest { loadMessages(it, false) }
    private val deviceMessages = authManager.clientId.flatMapLatest { loadMessages(it, true) }
    private val availableModelsFromProviders = credentialRepository.apiKeyCredentials.filterNotNull().map { credentials ->
        credentials.map { it.providerUrl to it.apiKey }
    }.mapToProvidedModels(pingDuration = 5.seconds)
    private val availableProvidersForModel = availableModelsFromProviders.mapToModelsAndProviders()

    private val llmService = LlmService(
        getResource = { resourceRepository.getResourceFlow(it, true).filterNotNull().first().toNetworkModel() },
        createResource = { chatId, modelId, resource ->
            val id = resourceRepository.upsert(resource.toDomainModel(), true)
            sendMessageWithResourceIds(chatId, modelId, resourceIds = listOf(id), deviceOnly = true)
            resource.copy(id = id)
        },
        upsertMessage = { message ->
            val id = sendMessageWithResourceIds(message.chatId, message.senderId, message.message, message.resourceIds, deviceOnly = true)
            message.copy(id = id)
        },
        getOpenAIClient = { endpoint ->
            val credential = try {
                credentialRepository.apiKeyCredentials.first()?.first { it.providerUrl == endpoint }!!
            } catch (e: NoSuchElementException) {
                Napier.e("No API key found for endpoint $endpoint", e)
                throw NoSuchElementException("No API key found for endpoint $endpoint")
            }
            provideOpenAIClient(credential)
        }
    )

    private val userResources = userMessages.flatMapLatest { messages ->
        // map to Flow<Map<String?, List<DomainResource?>>>
        combine(messages.filter { it.resourceIds.isNotEmpty() }.groupBy { it.id }.mapValues { (_, messages) ->
            combine(messages.map { message ->
                combine(message.resourceIds.map { loadResource(it, false) }) { it.toList() }
            }) { flows -> flows.toList().flatten().distinctBy { it?.id } }
        }.map { (key, value) -> value.map { key to it } }) { resources ->
            resources.toList().toMap()
        }
    }

    private val deviceResources = deviceMessages.flatMapLatest { messages ->
        // map to Flow<Map<String?, List<DomainResource?>>>
        combine(messages.filter { it.resourceIds.isNotEmpty() }.groupBy { it.id }.mapValues { (_, messages) ->
            combine(messages.map { message ->
                combine(message.resourceIds.map { loadResource(it, true) }) { it.toList() }
            }) { flows -> flows.toList().flatten().distinctBy { it?.id } }
        }.map { (key, value) -> value.map { key to it } }) { resources ->
            resources.toList().toMap()
        }
    }

    init {
        coroutineScopeIO.launch {
            // Update each chat on initialization
            val chats = deviceChats.first()
            val messages = deviceMessages.first()
            chats.forEach { chat ->
                chat.id?.let { answerOpenAIChat(it, messages) }
            }
        }
    }

    private fun loadChats(userId: String?, deviceOnly: Boolean): Flow<List<DomainChat>> {
        if (userId == null) {
            Napier.v { "User not signed in, reseting chats from ${if (deviceOnly) "Device" else "Server"}" }
            return flowOf(emptyList())
        }
        Napier.v { "Loading chats for user $userId" }
        return chatRepository.getUserChatsFlow(userId, deviceOnly)
    }

    private fun loadMessages(userId: String?, deviceOnly: Boolean): Flow<List<DomainMessage>> {
        if (userId == null) {
            Napier.v { "User not signed in, reseting messages from ${if (deviceOnly) "Device" else "Server"}" }
            return flowOf(emptyList())
        }
        Napier.v { "Loading messages for user $userId" }
        return messageRepository.getUserMessagesFlow(userId, deviceOnly)
    }

    private fun loadResource(resourceId: String, deviceOnly: Boolean): Flow<DomainResource?> {
        return resourceRepository.getResourceFlow(resourceId, deviceOnly)
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

    private suspend fun answerOpenAIChat(chatId: String, previousMessages: List<DomainMessage> = emptyList()) {
        val clientId = authManager.clientId.filterNotNull().first()
        val previousChats = chatRepository.getUserChatsFlow(clientId, true).first()
        val messages = (previousMessages + deviceMessages.first()).distinctBy { it.id }
        llmService.answerChat(
            chat = previousChats.first { it.id == chatId }.toNetworkModel().copy(
                lastFewMessages = messages.filter { it.chatId == chatId }.map { it.toNetworkModel() }
            ),
            previousChats = previousChats.filter { it.id != chatId }.map { it.toNetworkModel().copy(
                lastFewMessages = messages.filter { message -> message.chatId == it.id }.map { m -> m.toNetworkModel() }
            ) }
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

    suspend fun sendMessageWithResources(
        chatId: String,
        senderId: String,
        message: String? = null,
        resources: List<DomainResource>,
    ): String {
        val resourceIds = resources.map { resource ->
            resourceRepository.upsert(resource, deviceOnly = authManager.clientId.first() == senderId)
        }
        return messageRepository.upsert(
            DomainMessage(
                chatId = chatId,
                senderId = senderId,
                message = message,
                resourceIds = resourceIds
            ),
            deviceOnly = authManager.clientId.first() == senderId
        ).also {
            if (authManager.clientId.first() == senderId) {
                coroutineScopeIO.launch {
                    Napier.v { "Answering OpenAI chat" }
                    answerOpenAIChat(chatId)
                }
            }
        }
    }

    suspend fun sendMessage(
        chatId: String,
        senderId: String,
        message: String,
    ) = sendMessageWithResourceIds(chatId, senderId, message, emptyList(), deviceOnly = authManager.clientId.first() == senderId)

    suspend fun sendMessageWithResourceIds(
        chatId: String,
        senderId: String,
        message: String? = null,
        resourceIds: List<String>,
        deviceOnly: Boolean
    ) = messageRepository.upsert(
        DomainMessage(
            chatId = chatId,
            senderId = senderId,
            message = message,
            resourceIds = resourceIds
        ),
        deviceOnly = deviceOnly
    ).also {
        if (authManager.clientId.first() == senderId) {
            coroutineScopeIO.launch {
                answerOpenAIChat(chatId)
            }
        }
    }

    suspend fun sendAudioMessage(chatId: String, senderId: String, audioResource: DomainResource): String {
        return sendMessageWithResources(chatId, senderId, resources = listOf(audioResource))
    }

    suspend fun sendImageMessage(chatId: String, imagePath: String, senderId: String): String {
        val path = Path(imagePath)
        val imageContent = SystemFileSystem.source(path).buffered().readByteArray()
        val imageId = resourceRepository.upsert(
            DomainResource(
                type = "image/${path.name.substringAfterLast('.').lowercase()}",
                data = imageContent
            ),
            deviceOnly = authManager.clientId.first() == senderId
        )
        return sendMessageWithResourceIds(chatId, senderId, resourceIds = listOf(imageId), deviceOnly = authManager.clientId.first() == senderId)
    }

    suspend fun deleteChat(chatId: String) {
        val chatOwner = combine(userChats, deviceChats) { user, device -> user + device }.first().firstOrNull { it.id == chatId }?.ownerId
        val deviceOnly = authManager.clientId.first() == chatOwner
        Napier.v { "Deleting chat $chatId from ${if (deviceOnly) "Device" else "Server"}" }
        chatRepository.deleteChat(chatId, deviceOnly)
    }
}
