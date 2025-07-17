package illyan.butler.core.local.sql

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.room.TypeConverters
import illyan.butler.core.local.sql.dao.ApiKeyCredentialDao
import illyan.butler.core.local.sql.dao.ChatDao
import illyan.butler.core.local.sql.dao.DataHistoryDao
import illyan.butler.core.local.sql.dao.MessageDao
import illyan.butler.core.local.sql.dao.ResourceDao
import illyan.butler.core.local.sql.dao.UserDao
import illyan.butler.core.local.sql.dao.UserTokensDao
import illyan.butler.core.local.sql.model.RoomApiKeyCredential
import illyan.butler.core.local.sql.model.RoomChat
import illyan.butler.core.local.sql.model.RoomDataHistory
import illyan.butler.core.local.sql.model.RoomMessage
import illyan.butler.core.local.sql.model.RoomResource
import illyan.butler.core.local.sql.model.RoomUser
import illyan.butler.core.local.sql.model.RoomUserTokens

@Suppress("NO_ACTUAL_FOR_EXPECT")
expect object ButlerDatabaseCtor : RoomDatabaseConstructor<ButlerDatabase> {
    override fun initialize(): ButlerDatabase
}

const val BUTLER_DATABASE_VERSION = 12

@Database(
    entities = [
        RoomChat::class,
        RoomMessage::class,
        RoomDataHistory::class,
        RoomResource::class,
        RoomUser::class,
        RoomApiKeyCredential::class,
        RoomUserTokens::class
    ],
    version = BUTLER_DATABASE_VERSION,
    autoMigrations = [],
    exportSchema = true
)
@ConstructedBy(ButlerDatabaseCtor::class)
@TypeConverters(Converters::class)
abstract class ButlerDatabase : RoomDatabase() {
    abstract fun chatDao(): ChatDao
    abstract fun messageDao(): MessageDao
    abstract fun dataHistoryDao(): DataHistoryDao
    abstract fun resourceDao(): ResourceDao
    abstract fun apiKeyCredentialDao(): ApiKeyCredentialDao
    abstract fun userTokensDao(): UserTokensDao
    abstract fun userDao(): UserDao
}
