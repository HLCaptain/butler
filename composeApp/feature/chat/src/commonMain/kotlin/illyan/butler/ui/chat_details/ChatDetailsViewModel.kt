package illyan.butler.ui.chat_details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import illyan.butler.auth.AuthManager
import illyan.butler.chat.ChatManager
import illyan.butler.model.ModelManager
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
import org.koin.android.annotation.KoinViewModel

@KoinViewModel
class ChatDetailsViewModel(
    private val chatManager: ChatManager,
    private val authManager: AuthManager,
    modelManager: ModelManager
) : ViewModel() {
    private val currentChatId = MutableStateFlow<String?>(null)
    @OptIn(ExperimentalCoroutinesApi::class)
    private val currentChat = currentChatId.flatMapLatest { chatId ->
        chatId?.let { chatManager.getChatFlow(chatId) } ?: flowOf(null)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    @OptIn(ExperimentalCoroutinesApi::class)
    private val deviceOnly = currentChat.flatMapLatest { chat ->
        authManager.clientId.map { it == chat?.ownerId }
    }

    val state = combine(
        currentChat,
        modelManager.getAvailableModelsFromProviders(),
        deviceOnly
    ) { currentChat, providerModels, device ->
        ChatDetailsState(
            chat = currentChat,
            alternativeModels = if (device) providerModels.map { it.endpoint to it.id } else emptyList()
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, ChatDetailsState())

    fun loadChat(chatId: String?) {
        currentChatId.update { chatId }
    }

    fun setAudioTranscriptionModel(model: Pair<String, String>?) {
        viewModelScope.launch {
            chatManager.setAudioTranscriptionModel(currentChatId.value!!, model)
        }
    }

    fun setAudioTranslationModel(model: Pair<String, String>?) {
        viewModelScope.launch {
            chatManager.setAudioTranslationModel(currentChatId.value!!, model)
        }
    }

    fun setAudioSpeechModel(model: Pair<String, String>?) {
        viewModelScope.launch {
            chatManager.setAudioSpeechModel(currentChatId.value!!, model)
        }
    }

    fun setImageGenerationsModel(model: Pair<String, String>?) {
        viewModelScope.launch {
            chatManager.setImageGenerationsModel(currentChatId.value!!, model)
        }
    }
}