package illyan.butler.ui.new_chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import illyan.butler.chat.ChatManager
import illyan.butler.model.ModelManager
import illyan.butler.shared.model.chat.AiSource
import illyan.butler.shared.model.chat.Source
import io.github.aakira.napier.Napier
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
@KoinViewModel
class NewChatViewModel(
    modelManager: ModelManager,
    private val chatManager: ChatManager
) : ViewModel() {
    private val models = modelManager.getAiSources()
    private val creatingNewChat = MutableStateFlow(false)
    private val newChatId = MutableStateFlow<Uuid?>(null)

    val state = combine(
        models,
        creatingNewChat,
        newChatId
    ) { flows ->
        val flows = flows.toMutableList()
        val aiSources = flows.removeAt(0) as? List<AiSource>
        val isCreating = flows.removeAt(0) as Boolean
        val newId = flows.removeAt(0) as? Uuid
        NewChatState(
            aiSources = aiSources,
            // Local models are not yet implemented, so we give back null for now
            creatingChat = isCreating,
            newChatId = newId
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = NewChatState()
    )

    fun createChatWithModel(
        aiSource: AiSource,
        source: Source,
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            Napier.v { "Creating new chat with modelId: ${aiSource.modelId}, endpoint: ${aiSource.endpoint}" }
            creatingNewChat.update { true }
            val id = chatManager.startNewChat(source, aiSource)
            creatingNewChat.update { false }
            newChatId.update { id }
            Napier.v { "New chat created with id: $id" }
        }
    }

    fun clearNewChatId() {
        newChatId.update { null }
    }
}
