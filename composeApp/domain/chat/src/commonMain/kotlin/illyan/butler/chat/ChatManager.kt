package illyan.butler.chat

import illyan.butler.auth.AuthManager
import illyan.butler.core.network.ktor.http.di.provideOpenAIClient
import illyan.butler.core.network.mapping.toDomainModel
import illyan.butler.core.network.mapping.toNetworkModel
import illyan.butler.data.chat.ChatRepository
import illyan.butler.data.credential.CredentialRepository
import illyan.butler.data.error.ErrorRepository
import illyan.butler.data.message.MessageRepository
import illyan.butler.data.resource.ResourceRepository
import illyan.butler.di.KoinNames
import illyan.butler.domain.model.DomainChat
import illyan.butler.domain.model.DomainMessage
import illyan.butler.domain.model.DomainResource
import illyan.butler.domain.model.ErrorCode
import illyan.butler.shared.llm.LlmService
import illyan.butler.shared.llm.mapToModelsAndProviders
import illyan.butler.shared.llm.mapToProvidedModels
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
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
    private val credentialRepository: CredentialRepository,
    private val errorRepository: ErrorRepository
) {
    val userChats = authManager.signedInUserId.flatMapLatest { loadChats(it, false) }
        .stateIn(
            coroutineScopeIO,
            SharingStarted.Eagerly,
            emptyList()
        )
    val deviceChats = authManager.clientId.flatMapLatest { loadChats(it, true) }
        .stateIn(
            coroutineScopeIO,
            SharingStarted.Eagerly,
            emptyList()
        )
    private val userMessages = authManager.signedInUserId.flatMapLatest { loadMessages(it, false) }
        .stateIn(
            coroutineScopeIO,
            SharingStarted.Eagerly,
            emptyList()
        )
    private val deviceMessages = authManager.clientId.flatMapLatest { loadMessages(it, true) }
        .stateIn(
            coroutineScopeIO,
            SharingStarted.Eagerly,
            emptyList()
        )

    private val availableModelsFromProviders = credentialRepository.apiKeyCredentials.filterNotNull().map { credentials ->
        credentials.map { it.providerUrl to it.apiKey }
    }.mapToProvidedModels(pingDuration = 5.seconds)
    private val availableProvidersForModel = availableModelsFromProviders.mapToModelsAndProviders()

    private val llmService = LlmService(
        coroutineScopeIO = coroutineScopeIO,
        getResource = { resourceRepository.getResourceFlow(it, true).filterNotNull().first().toNetworkModel() },
        createResource = { chatId, modelId, resource ->
            val id = resourceRepository.upsert(resource.toDomainModel(), true)
            sendMessageWithResourceIds(
                DomainMessage(
                    chatId = chatId,
                    senderId = modelId,
                    resourceIds = listOf(id)
                ),
                deviceOnly = true
            )
            resource.copy(id = id)
        },
        upsertMessage = { message ->
            val id = sendMessageWithResourceIds(message.toDomainModel(), deviceOnly = true)
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
        },
        upsertChat = { chat ->
            chatRepository.upsert(chat.toDomainModel(), true)
            chat
        },
        errorInMessageResponse = { message ->
            errorRepository.reportSimpleError(ErrorCode.MessageResponseError)
            message?.toDomainModel()?.let { messageRepository.delete(it, deviceOnly = true) }
        }
    )

    private val userResources = userMessages.flatMapLatest { messages ->
        // map to Flow<List<DomainResource>>
        if (messages.isEmpty()) {
            Napier.v { "No messages, resetting user resources" }
            return@flatMapLatest flowOf(emptyList())
        }
        combine(messages.map { it.resourceIds }.flatten().distinct().map { loadResource(it, false) }) {
            Napier.v { "User resources: ${it.toList().map { it?.id }}" }
            it.toList()
        }
    }.stateIn(
        coroutineScopeIO,
        SharingStarted.Eagerly,
        emptyList()
    )

    private val deviceResources = deviceMessages.flatMapLatest { messages ->
        // map to Flow<List<DomainResource>>
        if (messages.isEmpty()) {
            Napier.v { "No messages, resetting user resources" }
            return@flatMapLatest flowOf(emptyList())
        }
        combine(messages.map { it.resourceIds }.flatten().distinct().map { loadResource(it, true) }) {
            Napier.v { "Device resources: ${it.toList().map { it?.id }}" }
            it.toList()
        }
    }.stateIn(
        coroutineScopeIO,
        SharingStarted.Eagerly,
        emptyList()
    )

    private fun loadChats(userId: String?, deviceOnly: Boolean): Flow<List<DomainChat>> {
        if (userId == null) {
            Napier.v { "User not signed in, resetting chats from ${if (deviceOnly) "Device" else "Server"}" }
            return flowOf(emptyList())
        }
        Napier.v { "Loading chats for user $userId" }
        return chatRepository.getUserChatsFlow(userId, deviceOnly)
    }

    private fun loadMessages(userId: String?, deviceOnly: Boolean): Flow<List<DomainMessage>> {
        if (userId == null) {
            Napier.v { "User not signed in, resetting messages from ${if (deviceOnly) "Device" else "Server"}" }
            return flowOf(emptyList())
        }
        Napier.v { "Loading messages for user $userId" }
        return messageRepository.getUserMessagesFlow(userId, deviceOnly)
    }

    private fun loadResource(resourceId: String, deviceOnly: Boolean): Flow<DomainResource?> {
        return resourceRepository.getResourceFlow(resourceId, deviceOnly)
    }

    fun getChatFlow(chatId: String) = combine(userChats, deviceChats) { user, device -> (user + (device ?: emptyList())).firstOrNull { it.id == chatId } }
    fun getMessagesByChatFlow(chatId: String) = combine(userMessages, deviceMessages) { user, device -> (user + (device ?: emptyList())).filter { it.chatId == chatId } }
    fun getResource(resourceId: String) = combine(userResources, deviceResources) { user, device -> (user + device).firstOrNull { it?.id == resourceId } }
    fun getResources(resourceIds: List<String>) = combine(userResources, deviceResources) { user, device -> (user + device).filter { resource -> resource?.id?.let { resourceIds.contains(it) } ?: false } }
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
                messageContent = message,
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
    ) = sendMessageWithResourceIds(
        message = DomainMessage(
            chatId = chatId,
            senderId = senderId,
            messageContent = message,
            resourceIds = emptyList()
        ),
        deviceOnly = authManager.clientId.first() == senderId
    )

    suspend fun sendMessageWithResourceIds(
        message: DomainMessage,
        deviceOnly: Boolean
    ) = messageRepository.upsert(
        message,
        deviceOnly = deviceOnly
    ).also {
        if (authManager.clientId.first() == message.senderId) {
            coroutineScopeIO.launch {
                answerOpenAIChat(message.chatId)
            }
        }
    }

    suspend fun sendAudioMessage(chatId: String, senderId: String, audioResource: DomainResource): String {
        return sendMessageWithResources(chatId, senderId, resources = listOf(audioResource))
    }

    suspend fun sendImageMessage(chatId: String, imageContent: ByteArray, mimeType: String, senderId: String): String {
        val imageId = resourceRepository.upsert(
            DomainResource(
                type = mimeType,
                data = imageContent
            ),
            deviceOnly = authManager.clientId.first() == senderId
        )
        return sendMessageWithResourceIds(
            DomainMessage(
                chatId = chatId,
                senderId = senderId,
                resourceIds = listOf(imageId)
            ),
            deviceOnly = authManager.clientId.first() == senderId
        )
    }

    suspend fun deleteChat(chatId: String) {
        val chatOwner = combine(userChats, deviceChats) { user, device -> user + device }.first().firstOrNull { it.id == chatId }?.ownerId
        val deviceOnly = authManager.clientId.first() == chatOwner
        Napier.v { "Deleting chat $chatId from ${if (deviceOnly) "Device" else "Server"}" }
        chatRepository.deleteChat(chatId, deviceOnly)
    }

    suspend fun setAudioTranscriptionModel(chatId: String, model: Pair<String, String>?) {
        combine(userChats, deviceChats) { user, device -> user + device }.first().firstOrNull { it.id == chatId }?.let { chat ->
            chatRepository.upsert(
                chat.copy(
                    audioTranscriptionModel = model
                ),
                deviceOnly = authManager.clientId.first() == chat.ownerId
            )
        }
    }

    suspend fun setAudioTranslationModel(chatId: String, model: Pair<String, String>?) {
        combine(userChats, deviceChats) { user, device -> user + device }.first().firstOrNull { it.id == chatId }?.let { chat ->
            chatRepository.upsert(
                chat.copy(
                    audioTranslationModel = model
                ),
                deviceOnly = authManager.clientId.first() == chat.ownerId
            )
        }
    }

    suspend fun setAudioSpeechModel(chatId: String, model: Pair<String, String>?) {
        combine(userChats, deviceChats) { user, device -> user + device }.first().firstOrNull { it.id == chatId }?.let { chat ->
            chatRepository.upsert(
                chat.copy(
                    audioSpeechModel = model
                ),
                deviceOnly = authManager.clientId.first() == chat.ownerId
            )
        }
    }

    suspend fun setImageGenerationsModel(chatId: String, model: Pair<String, String>?) {
        combine(userChats, deviceChats) { user, device -> user + device }.first().firstOrNull { it.id == chatId }?.let { chat ->
            chatRepository.upsert(
                chat.copy(
                    imageGenerationsModel = model
                ),
                deviceOnly = authManager.clientId.first() == chat.ownerId
            )
        }
    }
}
