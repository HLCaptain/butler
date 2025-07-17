package illyan.butler.core.local.sql

import androidx.room.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import org.koin.core.annotation.Single

@Single
fun provideRoomDatabase(
    builder: RoomDatabase.Builder<ButlerDatabase>
): ButlerDatabase {
    return builder
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.IO)
        .build()
}

@Single
fun provideRoomDatabaseBuilder(): RoomDatabase.Builder<ButlerDatabase> = getPlatformRoomDatabaseBuilder()

expect fun getPlatformRoomDatabaseBuilder(): RoomDatabase.Builder<ButlerDatabase>

@Single
fun getChatDao(database: ButlerDatabase) = database.chatDao()

@Single
fun getMessageDao(database: ButlerDatabase) = database.messageDao()

@Single
fun getDataHistoryDao(database: ButlerDatabase) = database.dataHistoryDao()

@Single
fun getResourceDao(database: ButlerDatabase) = database.resourceDao()

@Single
fun getApiKeyCredentialDao(database: ButlerDatabase) = database.apiKeyCredentialDao()

@Single
fun getUserDao(database: ButlerDatabase) = database.userDao()

@Single
fun getUserTokensDao(database: ButlerDatabase) = database.userTokensDao()
