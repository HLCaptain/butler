package illyan.butler.data.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
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

@OptIn(ExperimentalUuidApi::class)
@Single
class AppSettingsLocalRepository(
    private val datastore: DataStore<Preferences>,
) : AppRepository {
    companion object {
        val appSettingsKey = stringPreferencesKey("app_settings")
        val selectedPromptConfigurationKey = stringPreferencesKey("selected_prompt_configuration")
        val hostKey = stringPreferencesKey("host")
        val signedInUserKey = stringPreferencesKey("signed_in_user")
        val defaultModelKey = stringPreferencesKey("default_model")
    }

    // allowStructuredMapKeys is required by Map<FilterOptions, Boolean> in FilterConfiguration
    val filterConfigurationJsonParser = Json { allowStructuredMapKeys = true }
    override val appSettings: Flow<AppSettings> = datastore.data.map { preferences ->
        if (preferences[appSettingsKey] != null) {
            try {
                filterConfigurationJsonParser.decodeFromString(preferences[appSettingsKey]!!)
            } catch (e: Exception) {
                Napier.e(e) { "Error decoding app settings, resetting to default" }
                datastore.edit { datastorePreferences ->
                    datastorePreferences[appSettingsKey] = filterConfigurationJsonParser.encodeToString(AppSettings.Default)
                }
                AppSettings.Default
            }
        } else {
            datastore.edit { datastorePreferences ->
                datastorePreferences[appSettingsKey] = filterConfigurationJsonParser.encodeToString(AppSettings.Default)
            }
            AppSettings.Default
        }
    }
    override val currentHost: Flow<String?> = datastore.data.map { preferences ->
        preferences[hostKey]
    }
    override val signedInServers: Flow<Set<Source.Server>> = datastore.data.map { preferences ->
        preferences[signedInUserKey]?.let { Json.decodeFromString(it) } ?: emptySet()
    }

    override val defaultModel: Flow<AiSource?> = datastore.data.map { preferences ->
        if (preferences[defaultModelKey] != null) {
            try {
                Json.decodeFromString(preferences[defaultModelKey]!!)
            } catch (e: Exception) {
                Napier.e(e) { "Error decoding default model, resetting to default" }
                datastore.edit { datastorePreferences ->
                    datastorePreferences.remove(defaultModelKey)
                }
                null
            }
        } else {
            datastore.edit { datastorePreferences ->
                datastorePreferences.remove(defaultModelKey)
            }
            null
        }
    }

    override suspend fun setUserPreferences(preferences: DomainPreferences) {
        datastore.edit { datastorePreferences ->
            appSettings.first()?.copy(preferences = preferences)?.let {
                datastorePreferences[appSettingsKey] = filterConfigurationJsonParser.encodeToString(it)
            }
        }
    }

    override suspend fun addServerSource(source: Source.Server) {
        datastore.edit { datastorePreferences ->
            val currentSources = datastorePreferences[signedInUserKey]?.let {
                Json.decodeFromString<Set<Source.Server>>(it)
            } ?: emptySet()
            val updatedSources = currentSources + source
            datastorePreferences[signedInUserKey] = Json.encodeToString(updatedSources)
        }
    }

    override suspend fun removeServerSource(source: Source.Server) {
        datastore.edit { datastorePreferences ->
            val currentSources = datastorePreferences[signedInUserKey]?.let {
                Json.decodeFromString<Set<Source.Server>>(it)
            } ?: emptySet()
            val updatedSources = currentSources - source
            if (updatedSources.isEmpty()) {
                datastorePreferences.remove(signedInUserKey)
            } else {
                datastorePreferences[signedInUserKey] = Json.encodeToString(updatedSources)
            }
        }
    }

    override suspend fun setDefaultModel(model: AiSource?) {
        datastore.edit { datastorePreferences ->
            if (model != null) {
                datastorePreferences[defaultModelKey] = Json.encodeToString(model)
            } else {
                datastorePreferences.remove(defaultModelKey)
            }
        }
    }

    override suspend fun setFilterConfiguration(filterConfiguration: FilterConfiguration) {
        datastore.edit { datastorePreferences ->
            appSettings.first().copy(filterConfiguration = filterConfiguration).let {
                datastorePreferences[appSettingsKey] = filterConfigurationJsonParser.encodeToString(it)
            }
        }
    }

    override suspend fun setAppSettings(appSettings: AppSettings) {
        datastore.edit { datastorePreferences ->
            datastorePreferences[appSettingsKey] = filterConfigurationJsonParser.encodeToString(appSettings)
        }
    }

    override val selectedPromptConfiguration: Flow<PromptConfiguration?>
        get() = datastore.data.map { preferences ->
            if (preferences[selectedPromptConfigurationKey] != null) {
                try {
                    Json.decodeFromString(preferences[selectedPromptConfigurationKey]!!)
                } catch (e: Exception) {
                    Napier.e(e) { "Error decoding selected prompt configuration, resetting to null" }
                    datastore.edit { datastorePreferences ->
                        datastorePreferences.remove(selectedPromptConfigurationKey)
                    }
                    null
                }
            } else {
                null
            }
        }

    override suspend fun setSelectedPromptConfiguration(promptConfiguration: PromptConfiguration?) {
        datastore.edit { datastorePreferences ->
            if (promptConfiguration != null) {
                datastorePreferences[selectedPromptConfigurationKey] = Json.encodeToString(promptConfiguration)
            } else {
                datastorePreferences.remove(selectedPromptConfigurationKey)
            }
        }
    }

    override suspend fun setPromptConfigurations(promptConfigurations: List<PromptConfiguration>) {
        datastore.edit { datastorePreferences ->
            appSettings.first().copy(promptConfigurations = promptConfigurations).let {
                datastorePreferences[appSettingsKey] =
                    filterConfigurationJsonParser.encodeToString(it)
            }
        }
    }
}
