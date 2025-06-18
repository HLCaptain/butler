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
import illyan.butler.shared.llm.LlmService
import illyan.butler.shared.model.chat.AiSource
import illyan.butler.shared.model.chat.SenderType
import illyan.butler.shared.model.chat.Source
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
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
) {
    val userChats = authManager.signedInServers.flatMapLatest { servers ->
        if (servers.isEmpty()) {
            Napier.v { "No signed in servers, resetting user chats" }
            return@flatMapLatest flowOf(emptyList())
        }
        Napier.v { "Loading user chats for servers: $servers" }
        combine(servers.map { loadChats(it) }) { it.toList().flatten() }
    }

    val deviceChats = authManager.clientId.filterNotNull().flatMapLatest { loadChats(Source.Device(it)) }

    val chats = combine(userChats, deviceChats) { user, device -> user + device }
        .stateIn(
            coroutineScopeIO,
            SharingStarted.Eagerly,
            emptyList()
        )
    private val userMessages = authManager.signedInServers.flatMapLatest { servers ->
        if (servers.isEmpty()) {
            Napier.v { "No signed in servers, resetting user messages" }
            return@flatMapLatest flowOf(emptyList())
        }
        Napier.v { "Loading user messages for servers: $servers" }
        combine(servers.map { loadMessages(it) }) { it.toList().flatten() }
    }

    private val deviceMessages = authManager.clientId.flatMapLatest { it?.let { loadMessages(Source.Device(it)) } ?: flowOf(emptyList()) }

    private val deviceSource = authManager.clientId.map { it?.let { Source.Device(it) } }

    private val llmService = LlmService(
        coroutineScopeIO = coroutineScopeIO,
        getResource = { resourceId, _ ->
            resourceRepository.getResourceFlow(
                resourceId,
                deviceSource.firstOrNull() ?: throw IllegalStateException("Device source is null")
            ).filterNotNull().first().toNetworkModel()
        },
        createResource = { chatId, sender, resource ->
            val deviceSource = deviceSource.firstOrNull() ?: throw IllegalStateException("Device source is null")
            val id = resourceRepository.upsert(resource.toDomainModel(deviceSource))
            sendMessageWithResourceIds(
                Message(
                    chatId = chatId,
                    sender = sender,
                    source = deviceSource,
                    resourceIds = listOf(id)
                ),
            )
            resource.copy(id = id)
        },
        upsertMessage = { message ->
            val id = sendMessageWithResourceIds(
                message.toDomainModel(deviceSource.firstOrNull() ?: throw IllegalStateException("Device source is null"))
            )
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
            chatRepository.upsert(chat.toDomainModel(deviceSource.firstOrNull() ?: throw IllegalStateException("Device source is null")))
            chat
        },
        errorInMessageResponse = { message ->
            errorRepository.reportSimpleError(ErrorCode.MessageResponseError)
            message?.toDomainModel(deviceSource.firstOrNull() ?: throw IllegalStateException("Device source is null"))?.let { messageRepository.delete(it) }
        },
        removeMessage = { message ->
            Napier.v { "Removing message ${message.id} from device" }
            messageRepository.delete(message.toDomainModel(deviceSource.firstOrNull() ?: throw IllegalStateException("Device source is null")))
        }
    )

    private val userResources = userMessages.flatMapLatest { messages ->
        // map to Flow<List<Resource>>
        if (messages.isEmpty()) {
            Napier.v { "No messages, resetting user resources" }
            return@flatMapLatest flowOf(emptyList())
        }
        combine(
            messages
                .groupBy { it.source }
                .map { (source, messages) ->
                    messages.map { message -> message.resourceIds.map { id -> loadResource(id, source) } }
                }.flatten().flatten()
        ) { flows ->
            Napier.v { "User resources: ${flows.toList().map { it?.id }}" }
            flows.toList()
        }
    }

    private val deviceResources = deviceMessages.flatMapLatest { messages ->
        // map to Flow<List<Resource>>
        if (messages.isEmpty()) {
            Napier.v { "No messages, resetting user resources" }
            return@flatMapLatest flowOf(emptyList())
        }
        combine(
            messages
                .groupBy { it.source }
                .map { (source, messages) ->
                    messages.map { message -> message.resourceIds.map { id -> loadResource(id, source) } }
                }.flatten().flatten()
        ) { flows ->
            Napier.v { "Device resources: ${flows.toList().map { it?.id }}" }
            flows.toList()
        }
    }

    private fun loadChats(source: Source?): Flow<List<Chat>> {
        if (source == null) {
            Napier.v { "Source is null, resetting chats" }
            return flowOf(emptyList())
        }
        Napier.v { "Loading chats for source $source" }
        return chatRepository.getChatFlowBySource(source).filterNotNull()
    }

    private fun loadMessages(source: Source?): Flow<List<Message>> {
        if (source == null) {
            Napier.v { "User not signed in, resetting messages from $source" }
            return flowOf(emptyList())
        }
        Napier.v { "Loading messages for source $source" }
        return messageRepository.getMessagesBySource(source)
    }

    private fun loadResource(resourceId: Uuid, source: Source): Flow<Resource?> {
        return resourceRepository.getResourceFlow(resourceId, source)
    }

    fun getChatFlow(chatId: Uuid) = chats.map { it.firstOrNull { chat -> chat.id == chatId } }
    fun getMessagesByChatFlow(chat: Chat) = messageRepository.getChatMessagesFlow(chat.id, chat.source)
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
        val chat = getChatFlow(chatId).first() ?: run {
            Napier.e("Chat with ID $chatId not found")
            return
        }
        val previousChats = chatRepository.getChatFlowBySource(chat.source).first().orEmpty()
        val messages = (previousMessages + deviceMessages.first()).distinctBy { it.id }
        llmService.answerChat(
            chat = chat.toNetworkModel(),
            chatMessages = messages.map { it.toNetworkModel() },
            previousChats = previousChats.filter { it.id != chatId }.map { it.toNetworkModel() }
        )
    }

    suspend fun nameChat(
        chatId: Uuid,
        name: String,
    ) {
        userChats.first().firstOrNull { it.id == chatId }?.let { chat ->
            chatRepository.upsert(chat.copy(title = name))
        }
    }

    suspend fun sendMessageWithResources(
        chatId: Uuid,
        sender: SenderType.User,
        message: String? = null,
        resources: List<Resource>,
    ): Uuid {
        val resourceIds = resources.map { resource ->
            resourceRepository.create(resource)
        }
        return messageRepository.create(
            Message(
                chatId = chatId,
                source = sender.source,
                sender = sender,
                content = message,
                resourceIds = resourceIds
            ),
        ).also {
            if (sender.source is Source.Device) {
                Napier.v { "Answering OpenAI chat" }
                answerOpenAIChat(chatId)
            }
        }
    }

    suspend fun sendMessage(
        chatId: Uuid,
        sender: SenderType.User,
        message: String,
    ) = sendMessageWithResourceIds(
        message = Message(
            chatId = chatId,
            sender = sender,
            source = sender.source,
            content = message,
        ),
    )

    suspend fun sendMessageWithResourceIds(
        message: Message,
    ) = messageRepository.create(message).also {
        Napier.v { "Message sent with ID: $it" }
        if (message.source is Source.Device && message.sender is SenderType.User) {
            answerOpenAIChat(message.chatId)
        }
    }

    suspend fun sendAudioMessage(chatId: Uuid, sender: SenderType.User, audioResource: Resource): Uuid {
        return sendMessageWithResources(chatId, sender, resources = listOf(audioResource))
    }

    suspend fun sendImageMessage(
        chatId: Uuid,
        imageContent: ByteArray,
        mimeType: String,
        sender: SenderType.User
    ): Uuid {
        val imageId = resourceRepository.upsert(
            Resource(
                mimeType = mimeType,
                data = imageContent,
                source = sender.source,
            ),
        )
        return sendMessageWithResourceIds(
            Message(
                chatId = chatId,
                source = sender.source,
                sender = sender,
                resourceIds = listOf(imageId)
            ),
        )
    }

    suspend fun deleteChat(chat: Chat) {
        chatRepository.deleteChat(chat)
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
