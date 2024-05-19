package illyan.butler.ui.chat_list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import illyan.butler.manager.ChatManager
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import org.koin.android.annotation.KoinViewModel

@KoinViewModel
class ChatListViewModel(
    chatManager: ChatManager,
) : ViewModel() {
    val userChats = chatManager.userChats
        .stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            emptyList()
        )
}