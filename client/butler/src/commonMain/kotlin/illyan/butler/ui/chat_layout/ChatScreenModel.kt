package illyan.butler.ui.chat_layout

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import illyan.butler.manager.ChatManager
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import org.koin.core.annotation.Factory

@Factory
class ChatScreenModel(
    private val chatManager: ChatManager
) : ScreenModel {

    val state = chatManager.userChats.map { chats ->
        ChatState(
            chats = chats
        )
    }.stateIn(
        screenModelScope,
        SharingStarted.Eagerly,
        ChatState()
    )
}