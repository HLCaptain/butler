package illyan.butler.ui.chat_details

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
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
import org.koin.core.annotation.Factory

@Factory
class ChatDetailsScreenModel(
    private val chatManager: ChatManager,
    authManager: AuthManager
) : ScreenModel {
    private val _currentChatId = MutableStateFlow<String?>(null)
    @OptIn(ExperimentalCoroutinesApi::class)
    private val _currentChat = _currentChatId.flatMapLatest { chatId ->
        chatId?.let { chatManager.getChatFlow(chatId) } ?: flowOf(null)
    }.stateIn(screenModelScope, SharingStarted.Eagerly, null)

    val state = combine(
        _currentChat,
        authManager.signedInUserId
    ) { chat, userId ->
        ChatDetailsScreenState(
            chat = chat,
            userId = userId
        )
    }.stateIn(screenModelScope, SharingStarted.Eagerly, ChatDetailsScreenState())
    fun loadChat(chatId: String) {
        _currentChatId.update { chatId }
    }
}