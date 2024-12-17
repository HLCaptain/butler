package illyan.butler.core.local.room

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.room.TypeConverters
import illyan.butler.core.local.room.dao.AppSettingsDao
import illyan.butler.core.local.room.dao.ChatDao
import illyan.butler.core.local.room.dao.ChatMemberDao
import illyan.butler.core.local.room.dao.UserDao
import illyan.butler.core.local.room.model.RoomAppSettings
import illyan.butler.core.local.room.model.RoomChat
import illyan.butler.core.local.room.model.RoomChatMember
import illyan.butler.core.local.room.model.RoomDataHistory
import illyan.butler.core.local.room.model.RoomMessage
import illyan.butler.core.local.room.model.RoomResource
import illyan.butler.core.local.room.model.RoomUser
import illyan.butler.data.local.room.dao.DataHistoryDao
import illyan.butler.data.local.room.dao.MessageDao
import illyan.butler.data.local.room.dao.ResourceDao

@Suppress("NO_ACTUAL_FOR_EXPECT")
expect object ButlerDatabaseCtor : RoomDatabaseConstructor<ButlerDatabase>

@Database(
    entities = [
        RoomChat::class,
        RoomMessage::class,
        RoomDataHistory::class,
        RoomAppSettings::class,
        RoomResource::class,
        RoomChatMember::class,
        RoomUser::class
    ],
    version = 6
)
@ConstructedBy(ButlerDatabaseCtor::class)
@TypeConverters(Converters::class)
abstract class ButlerDatabase : RoomDatabase() {
    abstract fun chatDao(): ChatDao
    abstract fun messageDao(): MessageDao
    abstract fun dataHistoryDao(): DataHistoryDao
    abstract fun resourceDao(): ResourceDao
    abstract fun chatMemberDao(): ChatMemberDao
    abstract fun appSettingsDao(): AppSettingsDao
    abstract fun userDao(): UserDao
}
