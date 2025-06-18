package illyan.butler.data.settings

import illyan.butler.domain.model.AppSettings
import illyan.butler.domain.model.DomainPreferences
import illyan.butler.domain.model.FilterConfiguration
import illyan.butler.shared.model.chat.AiSource
import illyan.butler.shared.model.chat.PromptConfiguration
import illyan.butler.shared.model.chat.Source
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.koin.core.annotation.Single
import kotlin.uuid.ExperimentalUuidApi

@OptIn(ExperimentalUuidApi::class)
@Single
class AppMemoryRepository : AppRepository {
    private val _appSettings = MutableStateFlow(AppSettings.Default)
    private val _currentHost = MutableStateFlow<String?>(null)
    private val _currentSignedInUser = MutableStateFlow<Set<Source.Server>>(emptySet())
    private val _defaultModel = MutableStateFlow<AiSource?>(null)
    override val selectedPromptConfiguration = MutableStateFlow<PromptConfiguration?>(null)
    override val appSettings: Flow<AppSettings> = _appSettings.asStateFlow()
    override val currentHost: Flow<String?> = _currentHost.asStateFlow()
    override val signedInServers: Flow<Set<Source.Server>> = _currentSignedInUser.asStateFlow()
    override val defaultModel: Flow<AiSource?> = _defaultModel.asStateFlow()

    override suspend fun setUserPreferences(preferences: DomainPreferences) {
        _appSettings.update { it.copy(preferences = preferences) }
    }

    override suspend fun addServerSource(source: Source.Server) {
        _currentSignedInUser.update { it + source }
    }

    override suspend fun setDefaultModel(model: AiSource?) {
        _defaultModel.update { model }
    }

    override suspend fun removeServerSource(source: Source.Server) {
        _currentSignedInUser.update { it + source }
    }

    override suspend fun setFilterConfiguration(filterConfiguration: FilterConfiguration) {
        _appSettings.update { it.copy(filterConfiguration = filterConfiguration) }
    }

    override suspend fun setAppSettings(appSettings: AppSettings) {
        _appSettings.update { appSettings }
    }

    override suspend fun setSelectedPromptConfiguration(promptConfiguration: PromptConfiguration?) {
        selectedPromptConfiguration.update { promptConfiguration }
    }

    override suspend fun setPromptConfigurations(promptConfigurations: List<PromptConfiguration>) {
        _appSettings.update { it.copy(promptConfigurations = promptConfigurations) }
    }
}
