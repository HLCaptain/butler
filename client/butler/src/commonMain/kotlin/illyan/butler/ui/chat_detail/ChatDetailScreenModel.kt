package illyan.butler.ui.chat_detail

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import illyan.butler.di.KoinNames
import illyan.butler.manager.AudioManager
import illyan.butler.manager.AuthManager
import illyan.butler.manager.ChatManager
import io.github.aakira.napier.Napier
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

    val state = combine(
        chat,
        messages,
        authManager.signedInUserId,
        audioManager.isRecording,
    ) { chat, messages, userId, recording ->
        ChatDetailState(
            chat = chat,
            messages = messages,
            userId = userId,
            isRecording = recording,
            canRecordAudio = audioManager.canRecordAudio
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
        Napier.d("Sending image $path")
    }
}