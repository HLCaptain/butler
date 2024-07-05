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
        .fallbackToDestructiveMigration(dropAllTables = true)
//        .apply { if (BuildConfig.DEBUG) /* TODO: make migrations as default, fallback to destructive only in debug */ }
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

@Single
fun getResourceDao(database: ButlerDatabase) = database.resourceDao()

@Single
fun getChatMemberDao(database: ButlerDatabase) = database.chatMemberDao()

@Single
fun getAppSettingsDao(database: ButlerDatabase) = database.appSettingsDao()

@Single
fun getUserDao(database: ButlerDatabase) = database.userDao()
