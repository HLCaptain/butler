package illyan.butler.data.settings

import com.russhwolf.settings.ExperimentalSettingsApi
import com.russhwolf.settings.coroutines.FlowSettings
import illyan.butler.domain.model.AppSettings
import illyan.butler.domain.model.DomainPreferences
import illyan.butler.domain.model.FilterConfiguration
import illyan.butler.shared.model.chat.AiSource
import illyan.butler.shared.model.chat.PromptConfiguration
import illyan.butler.shared.model.chat.Source
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import org.koin.core.annotation.Single
import kotlin.uuid.ExperimentalUuidApi

@OptIn(ExperimentalUuidApi::class, ExperimentalSettingsApi::class)
@Single
class AppSettingsLocalRepository(
    private val settings: FlowSettings,
) : AppRepository {
    companion object {
        val appSettingsKey = "app_settings"
        val hostKey = "host"
        val serverSourcesKey = "server_sources"
        val defaultModelKey = "default_model"
    }

    // allowStructuredMapKeys is required by Map<FilterOptions, Boolean> in FilterConfiguration
    val filterConfigurationJsonParser = Json { allowStructuredMapKeys = true }

    override val appSettings: Flow<AppSettings> = settings.getStringOrNullFlow(appSettingsKey).map {
        it?.let { filterConfigurationJsonParser.decodeFromString<AppSettings>(it) } ?: AppSettings.Default
    }

    override val currentHost: Flow<String?> = settings.getStringOrNullFlow(hostKey)

    override val signedInServers: Flow<Set<Source.Server>> = settings.getStringOrNullFlow(serverSourcesKey).map {
        it?.let { filterConfigurationJsonParser.decodeFromString<Set<Source.Server>>(it) } ?: emptySet()
    }

    override val defaultModel: Flow<AiSource?> =
        settings.getStringOrNullFlow(defaultModelKey).map {
            it?.let { Json.decodeFromString<AiSource>(it) }
        }

    override suspend fun setUserPreferences(preferences: DomainPreferences) {
        Napier.d { "setUserPreferences: $preferences" }
        val currentSettings = appSettings.first()
        val newSettings = currentSettings.copy(preferences = preferences)
        setAppSettings(newSettings)
    }

    override suspend fun addServerSource(source: Source.Server) {
        Napier.d { "addServerSource: $source" }
        val currentSources = signedInServers.first().toMutableSet()
        currentSources.add(source)
        settings.putString(serverSourcesKey, filterConfigurationJsonParser.encodeToString(currentSources))
    }

    override suspend fun removeServerSource(source: Source.Server) {
        Napier.d { "removeServerSource: $source" }
        val currentSources = signedInServers.first().toMutableSet()
        currentSources.remove(source)
        settings.putString(serverSourcesKey, filterConfigurationJsonParser.encodeToString(currentSources))
    }

    override suspend fun setDefaultModel(model: AiSource?) {
        Napier.d { "setDefaultModel: $model" }
        if (model == null) {
            settings.remove(defaultModelKey)
        } else {
            settings.putString(defaultModelKey, Json.encodeToString(model))
        }
    }

    override suspend fun setFilterConfiguration(filterConfiguration: FilterConfiguration) {
        Napier.d { "setFilterConfiguration: $filterConfiguration" }
        val currentSettings = appSettings.first()
        val newSettings = currentSettings.copy(filterConfiguration = filterConfiguration)
        setAppSettings(newSettings)
    }

    override suspend fun setAppSettings(appSettings: AppSettings) {
        Napier.d { "setAppSettings: $appSettings" }
        settings.putString(appSettingsKey, filterConfigurationJsonParser.encodeToString(appSettings))
    }

    override suspend fun setSelectedPromptConfiguration(promptConfiguration: PromptConfiguration?) {
        Napier.d { "setSelectedPromptConfiguration: $promptConfiguration" }
        val currentSettings = appSettings.first()
        val newSettings = currentSettings.copy(selectedPromptConfiguration = promptConfiguration)
        setAppSettings(newSettings)
    }

    override suspend fun setPromptConfigurations(promptConfigurations: List<PromptConfiguration>) {
        Napier.d { "setPromptConfigurations: $promptConfigurations" }
        val currentSettings = appSettings.first()
        val newSettings = currentSettings.copy(promptConfigurations = promptConfigurations)
        setAppSettings(newSettings)
    }
}
