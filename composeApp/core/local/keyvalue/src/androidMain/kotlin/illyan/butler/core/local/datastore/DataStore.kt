package illyan.butler.core.local.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences

fun getDataStore(context: Context): DataStore<Preferences> = getDataStoreByPath(
    producePath = { context.filesDir.resolve(dataStoreFileName).absolutePath }
)
