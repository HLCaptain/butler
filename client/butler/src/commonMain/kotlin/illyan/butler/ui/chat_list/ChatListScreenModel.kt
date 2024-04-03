package illyan.butler.ui.chat_list

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import illyan.butler.di.KoinNames
import illyan.butler.manager.ChatManager
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Named

@Factory
class ChatListScreenModel(
    chatManager: ChatManager,
    @Named(KoinNames.DispatcherIO) dispatcherIO: CoroutineDispatcher
) : ScreenModel {
    init {
        screenModelScope.launch(dispatcherIO) { chatManager.loadChat() }
    }
    val userChats = chatManager.userChats
        .stateIn(
            screenModelScope,
            SharingStarted.Eagerly,
            emptyList()
        )
}