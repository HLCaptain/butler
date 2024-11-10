package illyan.butler.ui.chat_list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import illyan.butler.chat.ChatManager
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel

@KoinViewModel
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