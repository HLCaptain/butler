package illyan.butler.ui.new_chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import illyan.butler.chat.ChatManager
import illyan.butler.domain.model.DomainModel
import illyan.butler.model.ModelManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel

@KoinViewModel
class NewChatViewModel(
    private val modelManager: ModelManager,
    private val chatManager: ChatManager
) : ViewModel() {
    private val availableModels = MutableStateFlow(emptyMap<DomainModel, List<String>>())
    private val creatingNewChat = MutableStateFlow(false)
    private val newChatId = MutableStateFlow<String?>(null)

    init {
        viewModelScope.launch(Dispatchers.IO) {
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
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = NewChatState()
    )

    fun createChatWithModel(modelId: String, endpoint: String? = null) {
        viewModelScope.launch(Dispatchers.IO) {
            creatingNewChat.update { true }
            val id = chatManager.startNewChat(modelId, endpoint)
            creatingNewChat.update { false }
            newChatId.update { id }
        }
    }

    fun clearNewChatId() {
        newChatId.update { null }
    }
}
