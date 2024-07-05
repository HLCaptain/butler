package illyan.butler.data.store

import illyan.butler.data.local.datasource.DataHistoryLocalDataSource
import illyan.butler.data.local.model.DataHistory
import io.github.aakira.napier.Napier
import org.mobilenativefoundation.store.store5.Bookkeeper

fun<Key> provideBookkeeper(
    dataHistoryLocalDataSource: DataHistoryLocalDataSource,
    group: String,
    keyToUUID: (Key) -> String
) = Bookkeeper.by(
    getLastFailedSync = { key: Key ->
        dataHistoryLocalDataSource.getLastFailedTimestamp(keyToUUID(key))?.also {
            Napier.d("Get last failed sync for $key is $it")
        }
    },
    setLastFailedSync = { key, timestamp ->
        Napier.d("Setting last failed sync for $key to $timestamp")
        dataHistoryLocalDataSource.insertDataHistory(DataHistory(keyToUUID(key), timestamp, group))
        true
    },
    clear = { key ->
        Napier.d("Clearing last failed sync for $key")
        dataHistoryLocalDataSource.deleteDataHistory(keyToUUID(key))
        true
    },
    clearAll = {
        Napier.d("Clearing all last failed syncs for group $group")
        dataHistoryLocalDataSource.deleteDataHistoryByGroup(group)
        true
    }
)