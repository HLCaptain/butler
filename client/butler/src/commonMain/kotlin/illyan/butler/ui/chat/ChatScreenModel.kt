package illyan.butler.ui.chat

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import illyan.butler.di.NamedCoroutineDispatcherIO
import illyan.butler.manager.ChatManager
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.koin.core.annotation.Factory
import org.koin.core.annotation.InjectedParam

@Factory
class ChatScreenModel(
    @InjectedParam private val chatUUID: String,
    private val chatManager: ChatManager,
    @NamedCoroutineDispatcherIO private val dispatcherIO: CoroutineDispatcher
) : ScreenModel {
    val chat = chatManager.getChatFlow(chatUUID)
        .stateIn(
            screenModelScope,
            SharingStarted.Eagerly,
            null
        )

    val messages = chatManager.getMessagesByChatFlow(chatUUID)
        .map { messages -> messages?.sortedBy { it.timestamp }?.reversed() }
        .stateIn(
            screenModelScope,
            SharingStarted.Eagerly,
            null
        )


    fun sendMessage(message: String) {
        screenModelScope.launch(dispatcherIO) {
            chatManager.sendMessage(chatUUID, message)
        }
    }
}