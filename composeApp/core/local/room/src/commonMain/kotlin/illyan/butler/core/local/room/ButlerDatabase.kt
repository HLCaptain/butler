package illyan.butler.core.local.room

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.room.TypeConverters
import illyan.butler.core.local.room.dao.ApiKeyCredentialDao
import illyan.butler.core.local.room.dao.ChatDao
import illyan.butler.core.local.room.dao.DataHistoryDao
import illyan.butler.core.local.room.dao.MessageDao
import illyan.butler.core.local.room.dao.ResourceDao
import illyan.butler.core.local.room.dao.UserDao
import illyan.butler.core.local.room.dao.UserTokensDao
import illyan.butler.core.local.room.model.RoomApiKeyCredential
import illyan.butler.core.local.room.model.RoomChat
import illyan.butler.core.local.room.model.RoomDataHistory
import illyan.butler.core.local.room.model.RoomMessage
import illyan.butler.core.local.room.model.RoomResource
import illyan.butler.core.local.room.model.RoomUser
import illyan.butler.core.local.room.model.RoomUserTokens

@Suppress("NO_ACTUAL_FOR_EXPECT")
expect object ButlerDatabaseCtor : RoomDatabaseConstructor<ButlerDatabase>

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
    version = 11
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
