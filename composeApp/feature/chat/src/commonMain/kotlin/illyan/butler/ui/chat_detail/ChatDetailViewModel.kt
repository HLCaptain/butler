package illyan.butler.ui.chat_detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import illyan.butler.audio.AudioManager
import illyan.butler.audio.toWav
import illyan.butler.auth.AuthManager
import illyan.butler.chat.ChatManager
import illyan.butler.data.error.ErrorRepository
import illyan.butler.domain.model.Chat
import illyan.butler.domain.model.ErrorCode
import illyan.butler.domain.model.Message
import illyan.butler.domain.model.Resource
import illyan.butler.settings.SettingsManager
import illyan.butler.shared.model.chat.AiSource
import illyan.butler.shared.model.chat.SenderType
import illyan.butler.shared.model.chat.Source
import io.github.aakira.napier.Napier
import korlibs.audio.format.MP3
import korlibs.audio.sound.AudioData
import korlibs.time.seconds
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel
import kotlin.time.ExperimentalTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class, ExperimentalTime::class, ExperimentalCoroutinesApi::class)
@KoinViewModel
class ChatDetailViewModel(
    private val chatManager: ChatManager,
    private val audioManager: AudioManager,
    private val errorRepository: ErrorRepository,
    private val settingsManager: SettingsManager,
    private val authManager: AuthManager
) : ViewModel() {
    private val chatIdStateFlow = MutableStateFlow<Uuid?>(null)
    private val selectedNewChatModel = settingsManager.defaultModel

    val chat = chatIdStateFlow
        .flatMapLatest { chatId -> chatId?.let { chatManager.getChatFlow(chatId) } ?: flowOf(null) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val signedInServers = authManager.signedInServers

    val messages = chat
        .flatMapLatest { chat -> chat?.let { chatManager.getMessagesByChatFlow(chat) } ?: flowOf(null) }
        .map { messages -> messages?.sortedBy { it.createdAt }?.reversed() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val resources = messages.flatMapLatest { messages ->
        if (messages.isNullOrEmpty()) {
            Napier.d("No messages to get resources from")
            return@flatMapLatest flowOf(null)
        }
        Napier.d("Getting resources for messages: ${messages.map { it.id }}")
        chatManager.getResources(messages.map { it.resourceIds }.flatten()).map { resources ->
            Napier.d("Resources: ${resources.map { resource -> resource?.id }}")
            resources.filterNotNull()
        }
    }.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        null
    )

    val state = combine(
        chat,
        messages,
        audioManager.isRecording,
        audioManager.playingAudioId,
        resources,
        selectedNewChatModel,
    ) { flows ->
        val chat = flows[0] as Chat?
        val messages = flows[1] as List<Message>?
        val recording = flows[2] as? Boolean == true
        val playing = flows[3] as? Uuid
        val resources = flows[4] as List<Resource>?
        val selectedModel = flows[5] as AiSource?
        val sounds = resources?.filter { it.mimeType.startsWith("audio") }
            ?.associate {
                it.id to try { it.data.toAudioData(it.mimeType)!!.totalTime.seconds.toFloat() } catch (e: Exception) { Napier.e(e) { "Audio file encode error for audio $it" }; 0f }
            } ?: emptyMap()
        val images = resources?.filter { it.mimeType.startsWith("image") }?.associate { it.id to it.data } ?: emptyMap()
//        Napier.v {
//            """
//            ChatDetailState:
//            chat: ${chat?.id}
//            messages: ${messages?.map { it.id }}
//            isRecording: $recording
//            playingAudio: $playing
//            resources: ${resources?.map { it.id }}
//            sounds: ${sounds.keys}
//            images: ${images.keys}
//            """.trimIndent()
//        }
        ChatDetailState(
            chat = chat,
            messages = messages,
            isRecording = recording,
            playingAudio = playing,
            sounds = sounds,
            images = images,
            selectedNewChatModel = selectedModel,
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        ChatDetailState()
    )

    fun loadChat(chatId: Uuid?) {
        Napier.v { "Loading chat $chatId" }
        chatIdStateFlow.update { chatId }
    }

    fun selectNewChatModel(model: AiSource?) {
        Napier.v { "Selected new chat model: ${model?.modelId}" }
        viewModelScope.launch {
            settingsManager.setDefaultModel(model)
        }
    }

    private suspend fun createNewChat(source: Source): Uuid {
        val aiSource = selectedNewChatModel.filterNotNull().first()
        return chatManager.startNewChat(source, aiSource).also { loadChat(it) }
    }

    private suspend fun getSourceFromAiSource(aiSource: AiSource?): Source? {
        if (aiSource == null) {
            Napier.e("AiSource is null, cannot determine chat source")
            return null
        }
        return when (aiSource) {
            is AiSource.Api, is AiSource.Local -> Source.Device(authManager.clientId.filterNotNull().first())
            is AiSource.Server -> signedInServers.map { servers ->
                servers.first { server -> server.endpoint == aiSource.endpoint }
            }.first()
        }
    }

    fun sendMessage(content: String) {
        viewModelScope.launch {
            val source = state.value.chat?.source ?: getSourceFromAiSource(state.value.selectedNewChatModel) ?: run {
                Napier.e("Chat source is null, cannot send message")
                return@launch
            }
            val chatId = if (chatIdStateFlow.value == null) {
                createNewChat(source)
            } else {
                chatIdStateFlow.value ?: return@launch
            }
            chatManager.sendMessage(chatId, SenderType.User(source), content)
        }
    }

    fun toggleRecording(source: Source) {
        if (!audioManager.canRecordAudio) return
        viewModelScope.launch {
            if (state.value.isRecording) {
                val audioResource = audioManager.stopRecording()
                chatIdStateFlow.value?.let {
                    chatManager.sendAudioMessage(it, SenderType.User(source), audioResource)
                }
            } else {
                audioManager.startRecording(source)
            }
        }
    }

    fun sendImage(imageContent: ByteArray, mimeType: String) {
        viewModelScope.launch {
            val source = state.value.chat?.source ?: getSourceFromAiSource(state.value.selectedNewChatModel) ?: run {
                Napier.e("Chat source is null, cannot send image")
                return@launch
            }
            chatIdStateFlow.value?.let {
                chatManager.sendImageMessage(it, imageContent, mimeType, SenderType.User(source))
                Napier.d("Image sent")
            }
        }
    }

    fun playAudio(audioId: Uuid) {
        viewModelScope.launch {
            chat.value?.source?.let {
                audioManager.playAudio(audioId, it)
            } ?: run {
                Napier.e("Chat source is null, cannot play audio")
            }
        }
    }

    fun stopAudio() {
        viewModelScope.launch {
            audioManager.stopAudio()
        }
    }

    fun refreshChat() {
        viewModelScope.launch {
            chatManager.refreshDeviceChat(chatIdStateFlow.value ?: return@launch)
        }
    }

    fun sendError(code: ErrorCode) {
        viewModelScope.launch {
            errorRepository.reportSimpleError(code)
        }
    }
}

private suspend fun ByteArray.toAudioData(mimeType: String): AudioData? {
    return when (mimeType) {
        "audio/wav" -> toWav()
        "audio/mp3" -> MP3.decode(this)
        else -> null
    }
}