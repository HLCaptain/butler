package illyan.butler.ui.chat

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import illyan.butler.manager.ChatManager
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import org.koin.core.annotation.Factory
import org.koin.core.annotation.InjectedParam

@Factory
class ChatScreenModel(
    @InjectedParam chatUUID: String,
    chatManager: ChatManager
) : ScreenModel {
    val chat = chatManager.userChats.map { chats ->
        chats?.firstOrNull { it.uuid == chatUUID }
    }.stateIn(
        screenModelScope,
        SharingStarted.Eagerly,
        null
    )
}