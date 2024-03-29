package illyan.butler.ui.model_list

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import illyan.butler.di.KoinNames
import illyan.butler.domain.model.DomainModel
import illyan.butler.manager.ChatManager
import illyan.butler.manager.ModelManager
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Named

@Factory
class ModelListScreenModel(
    private val modelManager: ModelManager,
    private val chatManager: ChatManager,
    @Named(KoinNames.DispatcherIO) private val dispatcherIO: CoroutineDispatcher
) : ScreenModel {

    private val _availableModels = MutableStateFlow(emptyList<DomainModel>())
    val availableModels = _availableModels.asStateFlow()

    private val _newChatUUID = MutableStateFlow<String?>(null)
    val newChatUUID = _newChatUUID.asStateFlow()

    init {
        screenModelScope.launch(dispatcherIO) {
            _availableModels.update { modelManager.getAvailableModels() }
        }
    }

    fun startNewChat(modelUUID: String) {
        screenModelScope.launch(dispatcherIO) {
            _newChatUUID.update { chatManager.startNewChat(modelUUID) }
        }
    }

    fun onNavigateToChat() {
        _newChatUUID.update { null }
    }
}