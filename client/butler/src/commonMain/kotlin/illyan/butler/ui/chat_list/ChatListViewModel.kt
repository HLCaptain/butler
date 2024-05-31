package illyan.butler.ui.chat_list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import illyan.butler.manager.ChatManager
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ChatListViewModel(
    private val chatManager: ChatManager,
) : ViewModel() {
    val userChats = chatManager.userChats
        .stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            emptyList()
        )

    fun deleteChat(chatId: String) {
        viewModelScope.launch {
            chatManager.deleteChat(chatId)
        }
    }
}