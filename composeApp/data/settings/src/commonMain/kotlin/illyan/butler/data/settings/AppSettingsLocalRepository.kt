package illyan.butler.data.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import illyan.butler.domain.model.AppSettings
import illyan.butler.domain.model.DomainPreferences
import illyan.butler.domain.model.ModelConfig
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import org.koin.core.annotation.Single

@Single
class AppSettingsLocalRepository(
    private val datastore: DataStore<Preferences>,
) : AppRepository {
    companion object {
        val appSettingsKey = stringPreferencesKey("app_settings")
        val hostKey = stringPreferencesKey("host")
        val signedInUserKey = stringPreferencesKey("signed_in_user")
        val defaultModelKey = stringPreferencesKey("default_model")
    }
    override val appSettings: Flow<AppSettings?> = datastore.data.map { preferences ->
        if (preferences[appSettingsKey] != null) {
            try {
                Json.decodeFromString(preferences[appSettingsKey]!!)
            } catch (e: Exception) {
                Napier.e(e) { "Error decoding app settings, resetting to default" }
                datastore.edit { datastorePreferences ->
                    datastorePreferences[appSettingsKey] = Json.encodeToString(AppSettings.Default)
                }
                null
            }
        } else {
            datastore.edit { datastorePreferences ->
                datastorePreferences[appSettingsKey] = Json.encodeToString(AppSettings.Default)
            }
            null
        }
    }
    override val currentHost: Flow<String?> = datastore.data.map { preferences ->
        preferences[hostKey]
    }
    override val currentSignedInUserId: Flow<String?> = datastore.data.map { preferences ->
        preferences[signedInUserKey]
    }
    override val defaultModel: Flow<ModelConfig?> = datastore.data.map { preferences ->
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
                datastorePreferences[appSettingsKey] = Json.encodeToString(it)
            }
        }
    }

    override suspend fun setSignedInUser(userId: String?) {
        datastore.edit { datastorePreferences ->
            if (userId != null) {
                datastorePreferences[signedInUserKey] = userId
            } else {
                datastorePreferences.remove(signedInUserKey)
            }
        }
    }

    override suspend fun setDefaultModel(model: ModelConfig?) {
        datastore.edit { datastorePreferences ->
            if (model != null) {
                datastorePreferences[defaultModelKey] = Json.encodeToString(model)
            } else {
                datastorePreferences.remove(defaultModelKey)
            }
        }
    }
}
