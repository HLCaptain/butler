package illyan.butler.ui.chat_detail

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import illyan.butler.di.KoinNames
import illyan.butler.domain.model.DomainChat
import illyan.butler.domain.model.DomainMessage
import illyan.butler.domain.model.DomainResource
import illyan.butler.manager.AudioManager
import illyan.butler.manager.AuthManager
import illyan.butler.manager.ChatManager
import illyan.butler.utils.Wav
import illyan.butler.utils.toWav
import io.github.aakira.napier.Napier
import io.ktor.http.ContentType
import korlibs.time.seconds
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Named

@Factory
class ChatDetailScreenModel(
    private val chatManager: ChatManager,
    private val authManager: AuthManager,
    private val audioManager: AudioManager,
    @Named(KoinNames.DispatcherIO) private val dispatcherIO: CoroutineDispatcher
) : ScreenModel {
    private val chatIdStateFlow = MutableStateFlow<String?>(null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val chat = chatIdStateFlow
        .flatMapLatest { chatId -> chatId?.let { chatManager.getChatFlow(chatId) } ?: flowOf(null) }

    @OptIn(ExperimentalCoroutinesApi::class)
    val messages = chatIdStateFlow
        .flatMapLatest { chatId -> chatId?.let { chatManager.getMessagesByChatFlow(chatId) } ?: flowOf(null) }
        .map { messages -> messages?.sortedBy { it.time }?.reversed() }

    @OptIn(ExperimentalCoroutinesApi::class)
    private val resources = messages.flatMapLatest { messages ->
        combine((messages ?: emptyList()).map { message ->
            chatManager.getResourcesByMessageFlow(message.id!!)
        }) { flows ->
            val resources = flows.toList().filterNotNull().flatten()
            Napier.d("Resources: ${resources.map { resource -> resource?.id }}")
        }
    }

    val state = combine(
        chat,
        messages,
        authManager.signedInUserId,
        audioManager.isRecording,
        audioManager.isPlaying,
        resources
    ) { flows ->
        val chat = flows[0] as? DomainChat
        val messages = flows[1] as? List<DomainMessage>
        val userId = flows[2] as? String
        val recording = flows[3] as? Boolean ?: false
        val playing = flows[4] as? String
        val resources = flows[5] as? List<DomainResource>
        val sounds = resources?.filter { it.type == ContentType.Audio.Wav.toString() }
            ?.associate { it.id!! to it.data.toWav()!!.totalTime.seconds.toFloat() } ?: emptyMap()
        val images = resources?.filter { it.type.split('/')[0] == "image" }
            ?.associate { it.id!! to it.data } ?: emptyMap()
        ChatDetailState(
            chat = chat,
            messages = messages,
            userId = userId,
            isRecording = recording,
            canRecordAudio = audioManager.canRecordAudio,
            playingAudio = playing,
            sounds = sounds,
            images = images
        )
    }.stateIn(
        screenModelScope,
        SharingStarted.Eagerly,
        ChatDetailState(canRecordAudio = audioManager.canRecordAudio)
    )

    fun loadChat(chatId: String) {
        screenModelScope.launch(dispatcherIO) {
            chatIdStateFlow.update { chatId }
        }
    }

    val userId = authManager.signedInUserId

    fun sendMessage(message: String) {
        screenModelScope.launch(dispatcherIO) {
            chatIdStateFlow.value?.let { chatManager.sendMessage(it, message) }
        }
    }

    fun toggleRecording() {
        if (!audioManager.canRecordAudio) return
        screenModelScope.launch(dispatcherIO) {
            if (state.value.isRecording) {
                val audioId = audioManager.stopRecording()
                chatIdStateFlow.value?.let { chatManager.sendAudioMessage(it, audioId) }
            } else {
                audioManager.startRecording()
            }
        }
    }

    fun sendImage(path: String) {
        screenModelScope.launch(dispatcherIO) {
            chatIdStateFlow.value?.let {
                chatManager.sendImageMessage(it, path)
                Napier.d("Image sent: $path")
            }
        }
    }

    fun playAudio(audioId: String) {
        screenModelScope.launch {
            audioManager.playAudio(audioId)
        }
    }

    fun stopAudio() {
        screenModelScope.launch {
            audioManager.stopAudio()
        }
    }
}