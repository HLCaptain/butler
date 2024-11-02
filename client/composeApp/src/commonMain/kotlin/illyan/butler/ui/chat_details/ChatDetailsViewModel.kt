package illyan.butler.ui.chat_details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import illyan.butler.manager.AuthManager
import illyan.butler.manager.ChatManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

class ChatDetailsViewModel(
    private val chatManager: ChatManager,
    authManager: AuthManager
) : ViewModel() {
    private val _currentChatId = MutableStateFlow<String?>(null)
    @OptIn(ExperimentalCoroutinesApi::class)
    private val _currentChat = _currentChatId.flatMapLatest { chatId ->
        chatId?.let { chatManager.getChatFlow(chatId) } ?: flowOf(null)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val state = combine(
        _currentChat,
        authManager.signedInUserId
    ) { chat, userId ->
        ChatDetailsScreenState(
            chat = chat,
            userId = userId
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, ChatDetailsScreenState())
    fun loadChat(chatId: String?) {
        _currentChatId.update { chatId }
    }
}