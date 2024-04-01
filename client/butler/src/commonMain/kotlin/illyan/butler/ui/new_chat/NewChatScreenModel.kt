package illyan.butler.ui.new_chat

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import illyan.butler.di.KoinNames
import illyan.butler.domain.model.DomainModel
import illyan.butler.manager.ChatManager
import illyan.butler.manager.ModelManager
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Named

@Factory
class NewChatScreenModel(
    private val modelManager: ModelManager,
    @Named(KoinNames.DispatcherIO) private val dispatcherIO: CoroutineDispatcher,
    private val chatManager: ChatManager
) : ScreenModel {
    private val availableModels = MutableStateFlow(emptyList<DomainModel>())
    private val creatingNewChat = MutableStateFlow(false)
    private val newChatId = MutableStateFlow<String?>(null)

    init {
        screenModelScope.launch(dispatcherIO) {
            availableModels.update { modelManager.getAvailableModels() }
        }
    }

    val state = combine(
        availableModels,
        creatingNewChat,
        newChatId
    ) { models, creating, id ->
        NewChatState(
            availableModels = models,
            creatingChat = creating,
            newChatId = id
        )
    }.stateIn(
        scope = screenModelScope,
        started = SharingStarted.Eagerly,
        initialValue = NewChatState()
    )

    fun createChatWithModel(modelId: String) {
        screenModelScope.launch(dispatcherIO) {
            creatingNewChat.update { true }
            val id = chatManager.startNewChat(modelId)
            creatingNewChat.update { false }
            newChatId.update { id }
        }
    }
}