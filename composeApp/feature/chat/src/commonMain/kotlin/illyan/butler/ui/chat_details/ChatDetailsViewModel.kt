package illyan.butler.ui.chat_details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import illyan.butler.auth.AuthManager
import illyan.butler.chat.ChatManager
import illyan.butler.domain.model.Capability
import illyan.butler.model.ModelManager
import illyan.butler.shared.model.chat.AiSource
import illyan.butler.shared.model.chat.Source
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
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class, ExperimentalCoroutinesApi::class)
@KoinViewModel
class ChatDetailsViewModel(
    private val chatManager: ChatManager,
    private val authManager: AuthManager,
    modelManager: ModelManager
) : ViewModel() {
    private val currentChatId = MutableStateFlow<Uuid?>(null)
    private val currentChat = currentChatId.flatMapLatest { chatId ->
        chatId?.let { chatManager.getChatFlow(chatId) } ?: flowOf(null)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val deviceOnly = currentChat.map { it?.source is Source.Device }

    val state = combine(
        currentChat,
        modelManager.getAiSources(),
        deviceOnly
    ) { currentChat, providerModels, device ->
        ChatDetailsState(
            chat = currentChat,
            alternativeModels = if (device) providerModels else emptyList()
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, ChatDetailsState())

    fun loadChat(chatId: Uuid?) {
        currentChatId.update { chatId }
    }

    fun setModel(model: AiSource?, capability: Capability) {
        viewModelScope.launch {
            chatManager.setModel(currentChatId.value!!, model, capability)
        }
    }
}
