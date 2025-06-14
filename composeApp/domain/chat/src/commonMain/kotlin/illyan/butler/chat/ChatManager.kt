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
import illyan.butler.domain.model.Capability
import illyan.butler.domain.model.Chat
import illyan.butler.domain.model.ErrorCode
import illyan.butler.domain.model.Message
import illyan.butler.domain.model.Resource
import illyan.butler.model.ModelManager
import illyan.butler.shared.llm.LlmService
import illyan.butler.shared.model.chat.AiSource
import illyan.butler.shared.model.chat.Source
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
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single
import kotlin.time.ExperimentalTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalCoroutinesApi::class, ExperimentalUuidApi::class, ExperimentalTime::class)
@Single
class ChatManager(
    @Named(KoinNames.CoroutineScopeIO) private val coroutineScopeIO: CoroutineScope,
    private val authManager: AuthManager,
    private val chatRepository: ChatRepository,
    private val messageRepository: MessageRepository,
    private val resourceRepository: ResourceRepository,
    private val credentialRepository: CredentialRepository,
    private val errorRepository: ErrorRepository,
    private val modelManager: ModelManager
) {
    val userChats = authManager.signedInUserId.flatMapLatest { loadChats(it?.let { Source.Server }) }
        .stateIn(
            coroutineScopeIO,
            SharingStarted.Eagerly,
            emptyList()
        )
    val deviceChats = authManager.clientId.filterNotNull().flatMapLatest { loadChats(Source.Device(it)) }
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

    private val llmService = LlmService(
        coroutineScopeIO = coroutineScopeIO,
        getResource = { resourceId, _ -> resourceRepository.getResourceFlow(resourceId, Source.Device).filterNotNull().first().toNetworkModel() },
        createResource = { chatId, senderId, resource ->
            val id = resourceRepository.upsert(resource.toDomainModel())
            sendMessageWithResourceIds(
                Message(
                    chatId = chatId,
                    senderId = senderId,
                    resourceIds = listOf(id)
                ),
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
        },
        removeMessage = { message ->
            Napier.v { "Removing message ${message.id} from device" }
            messageRepository.delete(message.toDomainModel(), deviceOnly = true)
        }
    )

    private val userResources = userMessages.flatMapLatest { messages ->
        // map to Flow<List<Resource>>
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
        // map to Flow<List<Resource>>
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

    private fun loadChats(source: Source?): Flow<List<Chat>> {
        if (source == null) {
            Napier.v { "Source is null, resetting chats" }
            return flowOf(emptyList())
        }
        Napier.v { "Loading chats for source $source" }
        return chatRepository.getChatFlowBySource(source).filterNotNull()
    }

    private fun loadMessages(userId: String?, deviceOnly: Boolean): Flow<List<Message>> {
        if (userId == null) {
            Napier.v { "User not signed in, resetting messages from ${if (deviceOnly) "Device" else "Server"}" }
            return flowOf(emptyList())
        }
        Napier.v { "Loading messages for user $userId" }
        return messageRepository.getUserMessagesFlow(userId, deviceOnly)
    }

    private fun loadResource(resourceId: Uuid, source: Source): Flow<Resource?> {
        return resourceRepository.getResourceFlow(resourceId, source)
    }

    fun getChatFlow(chatId: String) = combine(userChats, deviceChats) { user, device -> (user + (device ?: emptyList())).firstOrNull { it.id == chatId } }
    fun getMessagesByChatFlow(chatId: String) = combine(userMessages, deviceMessages) { user, device -> (user + (device ?: emptyList())).filter { it.chatId == chatId } }
    fun getResource(resourceId: Uuid) = combine(userResources, deviceResources) { user, device -> (user + device).firstOrNull { it?.id == resourceId } }
    fun getResources(resourceIds: List<Uuid>) = combine(userResources, deviceResources) { user, device -> (user + device).filter { resource -> resource?.id?.let { resourceIds.contains(it) } ?: false } }
    suspend fun startNewChat(
        chatSource: Source,
        aiSource: AiSource
    ): Uuid {
        return chatRepository.upsert(
            Chat(
                source = chatSource,
                models = mapOf(
                    Capability.CHAT_COMPLETION to aiSource
                )
            ),
        )
    }

    private suspend fun answerOpenAIChat(chatId: Uuid, previousMessages: List<Message> = emptyList()) {
        val clientId = authManager.clientId.filterNotNull().first()
        val previousChats = chatRepository.getUserChatsFlow(clientId, true).first()
        val messages = (previousMessages + deviceMessages.first()).distinctBy { it.id }
        llmService.answerChat(
            chat = previousChats.first { it.id == chatId }.toNetworkModel(),
            chatMessages = messages.map { it.toNetworkModel() },
            previousChats = previousChats.filter { it.id != chatId }.map { it.toNetworkModel() }
        )
    }

    suspend fun nameChat(
        chatId: Uuid,
        name: String,
    ) {
        userChats.first().firstOrNull { it.id == chatId }?.let { chat ->
            chatRepository.upsert(chat.copy(name = name), authManager.clientId.first() == chat.ownerId)
        }
    }

    suspend fun sendMessageWithResources(
        chatId: Uuid,
        senderId: Uuid,
        message: String? = null,
        resources: List<Resource>,
    ): Uuid {
        val resourceIds = resources.map { resource ->
            resourceRepository.upsert(resource, deviceOnly = authManager.clientId.first() == senderId)
        }
        return messageRepository.upsert(
            Message(
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
        chatId: Uuid,
        senderId: Uuid,
        message: String,
    ) = sendMessageWithResourceIds(
        message = Message(
            chatId = chatId,
            senderId = senderId,
            messageContent = message,
            resourceIds = emptyList()
        ),
        deviceOnly = authManager.clientId.first() == senderId
    )

    suspend fun sendMessageWithResourceIds(
        message: Message,
    ) = messageRepository.upsert(
        message,
    ).also {
        if (message.source is Source.Device) {
            coroutineScopeIO.launch {
                answerOpenAIChat(message.chatId)
            }
        }
    }

    suspend fun sendAudioMessage(chatId: Uuid, senderId: Uuid, audioResource: Resource): String {
        return sendMessageWithResources(chatId, senderId, resources = listOf(audioResource))
    }

    suspend fun sendImageMessage(chatId: Uuid, imageContent: ByteArray, mimeType: String, senderId: Uuid): String {
        val imageId = resourceRepository.upsert(
            Resource(
                mimeType = mimeType,
                data = imageContent
            ),
        )
        return sendMessageWithResourceIds(
            Message(
                chatId = chatId,
                senderId = senderId,
                resourceIds = listOf(imageId)
            ),
        )
    }

    suspend fun deleteChat(chatId: String) {
        chatRepository.deleteChat(chatId)
    }

    suspend fun setModel(chatId: Uuid, model: AiSource?, capability: Capability) {
        combine(userChats, deviceChats) { user, device -> user + device }.first().firstOrNull { it.id == chatId }?.let { chat ->
            chatRepository.upsert(
                chat.copy(
                    models = if (model == null) {
                        chat.models - capability
                    } else {
                        chat.models + mapOf(capability to model)
                    }
                ),
            )
        }
    }

    suspend fun refreshDeviceChat(chatId: Uuid) {
        answerOpenAIChat(chatId = chatId)
    }
}
