package illyan.butler.ui.chat_detail

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import illyan.butler.di.KoinNames
import illyan.butler.manager.AuthManager
import illyan.butler.manager.ChatManager
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.koin.core.annotation.Factory
import org.koin.core.annotation.InjectedParam
import org.koin.core.annotation.Named

@Factory
class ChatDetailScreenModel(
    @InjectedParam private val chatUUID: String,
    private val chatManager: ChatManager,
    private val authManager: AuthManager,
    @Named(KoinNames.DispatcherIO) private val dispatcherIO: CoroutineDispatcher
) : ScreenModel {
    val chat = chatManager.getChatFlow(chatUUID)
        .stateIn(
            screenModelScope,
            SharingStarted.Eagerly,
            null
        )

    val messages = chatManager.getMessagesByChatFlow(chatUUID)
        .map { messages -> messages?.sortedBy { it.time }?.reversed() }
        .stateIn(
            screenModelScope,
            SharingStarted.Eagerly,
            null
        )

    val userId = authManager.signedInUserUUID

    fun sendMessage(message: String) {
        screenModelScope.launch(dispatcherIO) {
            chatManager.sendMessage(chatUUID, message)
        }
    }
}