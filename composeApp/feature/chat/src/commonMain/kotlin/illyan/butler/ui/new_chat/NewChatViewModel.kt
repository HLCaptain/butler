package illyan.butler.ui.new_chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import illyan.butler.domain.model.FilterConfiguration
import illyan.butler.model.ModelManager
import illyan.butler.settings.SettingsManager
import illyan.butler.shared.model.chat.AiSource
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
@KoinViewModel
class NewChatViewModel(
    modelManager: ModelManager,
    private val settingsManager: SettingsManager,
) : ViewModel() {
    private val models = modelManager.getAiSources()
    private val creatingNewChat = MutableStateFlow(false)
    private val newChatId = MutableStateFlow<Uuid?>(null)

    val state = combine(
        models,
        creatingNewChat,
        newChatId,
        settingsManager.deviceFilterConfigurations
    ) { flows ->
        val flows = flows.toMutableList()
        val aiSources = flows.removeAt(0) as? List<AiSource>
        val isCreating = flows.removeAt(0) as Boolean
        val newId = flows.removeAt(0) as? Uuid
        val filterConfiguration = flows.removeAt(0) as FilterConfiguration
        NewChatState(
            aiSources = aiSources,
            // Local models are not yet implemented, so we give back null for now
            creatingChat = isCreating,
            newChatId = newId,
            filterConfiguration = filterConfiguration
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = NewChatState()
    )

    fun onFilterChanged(filterConfiguration: FilterConfiguration) {
        viewModelScope.launch {
            Napier.v { "Filter changed: $filterConfiguration" }
            settingsManager.setFilterConfiguration(filterConfiguration)
        }
    }
}
