package illyan.butler.core.local.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import java.nio.file.Paths

fun getDataStore(): DataStore<Preferences> = getDataStore(
    producePath = { Paths.get(System.getProperty("user.home"), ".butler", dataStoreFileName).toString() }
)
