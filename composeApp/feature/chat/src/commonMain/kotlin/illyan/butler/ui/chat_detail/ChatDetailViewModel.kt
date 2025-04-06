package illyan.butler.ui.chat_detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import illyan.butler.audio.AudioManager
import illyan.butler.audio.toWav
import illyan.butler.auth.AuthManager
import illyan.butler.chat.ChatManager
import illyan.butler.data.error.ErrorRepository
import illyan.butler.domain.model.DomainChat
import illyan.butler.domain.model.DomainMessage
import illyan.butler.domain.model.DomainResource
import illyan.butler.domain.model.ErrorCode
import io.github.aakira.napier.Napier
import korlibs.audio.format.MP3
import korlibs.audio.sound.AudioData
import korlibs.time.seconds
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel

@KoinViewModel
class ChatDetailViewModel(
    private val chatManager: ChatManager,
    private val authManager: AuthManager,
    private val audioManager: AudioManager,
    private val errorRepository: ErrorRepository
) : ViewModel() {
    private val chatIdStateFlow = MutableStateFlow<String?>(null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val chat = chatIdStateFlow
        .flatMapLatest { chatId -> chatId?.let { chatManager.getChatFlow(chatId) } ?: flowOf(null) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val isChatDeviceOnly = combine(chat, authManager.clientId) { chat, client -> chat?.ownerId == client }

    @OptIn(ExperimentalCoroutinesApi::class)
    val messages = chatIdStateFlow
        .flatMapLatest { chatId -> chatId?.let { chatManager.getMessagesByChatFlow(chatId) } ?: flowOf(null) }
        .map { messages -> messages?.sortedBy { it.time }?.reversed() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    @OptIn(ExperimentalCoroutinesApi::class)
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
    ) { flows ->
        val chat = flows[0] as DomainChat?
        val messages = flows[1] as List<DomainMessage>?
        val recording = flows[2] as? Boolean == true
        val playing = flows[3] as? String
        val resources = flows[4] as List<DomainResource>?
        val sounds = resources?.filter { it.type.startsWith("audio") }
            ?.associate {
                it.id!! to try { it.data.toAudioData(it.type)!!.totalTime.seconds.toFloat() } catch (e: Exception) { Napier.e(e) { "Audio file encode error for audio $it" }; 0f }
            } ?: emptyMap()
        val images = resources?.filter { it.type.startsWith("image") }?.associate { it.id!! to it.data } ?: emptyMap()
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
            images = images
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        ChatDetailState()
    )

    fun loadChat(chatId: String) {
        Napier.v { "Loading chat $chatId" }
        chatIdStateFlow.update { chatId }
    }

    fun sendMessage(message: String) {
        viewModelScope.launch {
            chat.value?.let { chatManager.sendMessage(it.id!!, it.ownerId, message) }
        }
    }

    fun toggleRecording(senderId: String) {
        if (!audioManager.canRecordAudio) return
        viewModelScope.launch {
            if (state.value.isRecording) {
                val audioResource = audioManager.stopRecording()
                chatIdStateFlow.value?.let { chatManager.sendAudioMessage(it, senderId, audioResource = audioResource) }
            } else {
                audioManager.startRecording()
            }
        }
    }

    fun sendImage(imageContent: ByteArray, mimeType: String, senderId: String) {
        viewModelScope.launch {
            chatIdStateFlow.value?.let {
                chatManager.sendImageMessage(it, imageContent, mimeType, senderId)
                Napier.d("Image sent")
            }
        }
    }

    fun playAudio(audioId: String) {
        viewModelScope.launch {
            audioManager.playAudio(audioId, deviceOnly = state.value.chat?.ownerId == authManager.clientId.first())
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