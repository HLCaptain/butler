package illyan.butler.data.settings

import illyan.butler.domain.model.AppSettings
import illyan.butler.domain.model.DomainPreferences
import illyan.butler.domain.model.FilterConfiguration
import illyan.butler.shared.model.chat.AiSource
import illyan.butler.shared.model.chat.PromptConfiguration
import illyan.butler.shared.model.chat.Source
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.uuid.ExperimentalUuidApi

@OptIn(ExperimentalUuidApi::class)
interface AppRepository {
    val appSettings: Flow<AppSettings>
    val currentHost: Flow<String?>
    val signedInServers: Flow<Set<Source.Server>>
    val defaultModel: Flow<AiSource?>
    val selectedPromptConfiguration: Flow<PromptConfiguration?>
    val isUserSignedIn: Flow<Boolean>
        get() = signedInServers.map { it.isNotEmpty() }

    suspend fun setUserPreferences(preferences: DomainPreferences)
    suspend fun addServerSource(source: Source.Server)
    suspend fun removeServerSource(source: Source.Server)
    suspend fun setDefaultModel(model: AiSource?)
    suspend fun setFilterConfiguration(filterConfiguration: FilterConfiguration)
    suspend fun setAppSettings(appSettings: AppSettings)
    suspend fun setSelectedPromptConfiguration(promptConfiguration: PromptConfiguration?)
    suspend fun setPromptConfigurations(promptConfigurations: List<PromptConfiguration>)
}
