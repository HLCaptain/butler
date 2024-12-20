package illyan.butler.data.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import illyan.butler.domain.model.AppSettings
import illyan.butler.domain.model.DomainPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
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
    }
    override val appSettings: Flow<AppSettings?> = datastore.data.map { preferences ->
        preferences[appSettingsKey]?.let { Json.decodeFromString(it) }
    }
    override val currentHost: Flow<String?> = datastore.data.map { preferences ->
        preferences[hostKey]
    }
    override val currentSignedInUserId: Flow<String?> = datastore.data.map { preferences ->
        preferences[signedInUserKey]
    }

    override suspend fun setUserPreferences(preferences: DomainPreferences) {
        datastore.edit { datastorePreferences ->
            datastorePreferences[appSettingsKey] = Json.encodeToString(preferences)
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
}