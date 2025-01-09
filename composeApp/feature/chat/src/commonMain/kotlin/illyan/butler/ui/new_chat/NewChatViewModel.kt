package illyan.butler.ui.new_chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import illyan.butler.auth.AuthManager
import illyan.butler.chat.ChatManager
import illyan.butler.domain.model.DomainModel
import illyan.butler.model.ModelManager
import io.github.aakira.napier.Napier
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
    modelManager: ModelManager,
    authManager: AuthManager,
    private val chatManager: ChatManager
) : ViewModel() {
    private val serverModels = modelManager.getAvailableModelsFromServer().stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = null
    )
    private val providerModels = modelManager.getAvailableModelsFromProviders().stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = null
    )
    private val creatingNewChat = MutableStateFlow(false)
    private val newChatId = MutableStateFlow<String?>(null)

    val state = combine(
        serverModels,
        providerModels,
        authManager.signedInUserId,
        authManager.clientId,
        creatingNewChat,
        newChatId
    ) { flows ->
        val serverModels = flows[0] as? List<DomainModel>?
        val providerModels = flows[1] as? List<DomainModel>?
        val userId = flows[2] as? String?
        val clientId = flows[3] as? String?
        val isCreating = flows[4] as Boolean
        val newId = flows[5] as? String
        NewChatState(
            userId = userId,
            clientId = clientId,
            providerModels = providerModels,
            serverModels = serverModels,
            localModels = emptyList(),
            creatingChat = isCreating,
            newChatId = newId
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = NewChatState()
    )

    fun createChatWithModel(
        modelId: String,
        endpoint: String,
        senderId: String
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            Napier.v { "Creating new chat with modelId: $modelId, endpoint: $endpoint, senderId: $senderId" }
            creatingNewChat.update { true }
            val id = chatManager.startNewChat(modelId, endpoint, senderId)
            creatingNewChat.update { false }
            newChatId.update { id }
            Napier.v { "New chat created with id: $id" }
        }
    }

    fun clearNewChatId() {
        newChatId.update { null }
    }
}
