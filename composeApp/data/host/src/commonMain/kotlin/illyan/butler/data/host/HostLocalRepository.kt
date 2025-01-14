package illyan.butler.data.host

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Single

@Single
class HostLocalRepository(
    private val datastore: DataStore<Preferences>
) : HostRepository {
    companion object {
        val hostKey = stringPreferencesKey("host")
    }

    override val currentHost = datastore.data.map { it[hostKey] }

    override suspend fun upsertHostUrl(url: String?) {
        datastore.edit { datastorePreferences ->
            if (url != null) {
                datastorePreferences[hostKey] = url
            } else {
                datastorePreferences.remove(hostKey)
            }
        }
    }
}
