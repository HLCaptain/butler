package illyan.butler.data.store

import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import illyan.butler.data.sqldelight.DatabaseHelper
import illyan.butler.db.DataHistory
import org.mobilenativefoundation.store.store5.Bookkeeper

fun<Key> provideBookkeeper(
    databaseHelper: DatabaseHelper,
    originTable: String,
    keyToUUID: (Key) -> String
) = Bookkeeper.by(
    getLastFailedSync = { key: Key ->
        databaseHelper.queryAsOneOrNullFlow { it.dataHistoryQueries.select(keyToUUID(key)) }.map {
            Napier.d("Get last failed sync for $key is ${it?.timestamp}")
            it?.timestamp
        }.firstOrNull()
    },
    setLastFailedSync = { key, timestamp ->
        databaseHelper.withDatabase {
            Napier.d("Setting last failed sync for $key to $timestamp")
            it.dataHistoryQueries.upsert(DataHistory(keyToUUID(key), timestamp, originTable))
        }
        true
    },
    clear = { key ->
        databaseHelper.withDatabase {
            Napier.d("Clearing last failed sync for $key")
            it.dataHistoryQueries.delete(keyToUUID(key))
        }
        true
    },
    clearAll = {
        databaseHelper.withDatabase {
            Napier.d("Clearing all last failed syncs")
            it.dataHistoryQueries.deleteAllFromTable(originTable)
        }
        true
    }
)