package illyan.butler.ui.chat_list

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import illyan.butler.manager.ChatManager
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import org.koin.core.annotation.Factory

@Factory
class ChatListScreenModel(
    chatManager: ChatManager
) : ScreenModel {
    val userChatsPerModel = chatManager.userChatsPerModel
        .map { it ?: emptyMap() }
        .stateIn(
            screenModelScope,
            SharingStarted.Eagerly,
            emptyMap()
        )
}