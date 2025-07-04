package illyan.butler.core.local.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okio.Path.Companion.toPath

private lateinit var dataStore: DataStore<Preferences>

private val lock = Mutex()

// Source: https://github.com/android/kotlin-multiplatform-samples/blob/main/DiceRoller/shared/src/commonMain/kotlin/com/google/samples/apps/diceroller/createDataStore.kt
fun getDataStore(producePath: () -> String): DataStore<Preferences> = runBlocking {
    lock.withLock {
        if (::dataStore.isInitialized) {
            dataStore
        } else {
            PreferenceDataStoreFactory.createWithPath(produceFile = { producePath().toPath() })
                .also { dataStore = it }
        }
    }
}

internal const val dataStoreFileName = "butler.preferences_pb"
