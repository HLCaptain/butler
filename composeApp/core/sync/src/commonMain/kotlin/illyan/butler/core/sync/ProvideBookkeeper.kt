package illyan.butler.core.sync

import illyan.butler.core.local.datasource.DataHistoryLocalDataSource
import illyan.butler.core.local.model.DataHistory
import io.github.aakira.napier.Napier
import org.mobilenativefoundation.store.store5.Bookkeeper

fun<Key> provideBookkeeper(
    dataHistoryLocalDataSource: DataHistoryLocalDataSource,
    group: String,
    keyToId: (Key) -> String
) = Bookkeeper.by(
    getLastFailedSync = { key: Key ->
        dataHistoryLocalDataSource.getLastFailedTimestamp(keyToId(key))?.also {
            Napier.d("Get last failed sync for $key is $it")
        }
    },
    setLastFailedSync = { key, timestamp ->
        Napier.d("Setting last failed sync for $key to $timestamp")
        dataHistoryLocalDataSource.insertDataHistory(DataHistory(keyToId(key), timestamp, group))
        true
    },
    clear = { key ->
        Napier.d("Clearing last failed sync for $key")
        dataHistoryLocalDataSource.deleteDataHistory(keyToId(key))
        true
    },
    clearAll = {
        Napier.d("Clearing all last failed syncs for group $group")
        dataHistoryLocalDataSource.deleteDataHistoryByGroup(group)
        true
    }
)