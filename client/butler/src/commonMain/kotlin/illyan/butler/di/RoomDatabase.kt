package illyan.butler.di

import androidx.room.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import illyan.butler.data.room.ButlerDatabase
import kotlinx.coroutines.Dispatchers
import org.koin.core.annotation.Single

@Single
fun getRoomDatabase(
    builder: RoomDatabase.Builder<ButlerDatabase>
): ButlerDatabase {
    return builder
        .fallbackToDestructiveMigrationOnDowngrade(dropAllTables = true)
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.IO)
        .build()
}

@Single
expect fun getRoomDatabaseBuilder(): RoomDatabase.Builder<ButlerDatabase>

@Single
fun getChatDao(database: ButlerDatabase) = database.chatDao()

@Single
fun getMessageDao(database: ButlerDatabase) = database.messageDao()

@Single
fun getDataHistoryDao(database: ButlerDatabase) = database.dataHistoryDao()
