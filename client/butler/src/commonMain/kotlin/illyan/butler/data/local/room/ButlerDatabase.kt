package illyan.butler.data.local.room

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.room.TypeConverters
import illyan.butler.data.local.room.dao.AppSettingsDao
import illyan.butler.data.local.room.dao.ChatDao
import illyan.butler.data.local.room.dao.ChatMemberDao
import illyan.butler.data.local.room.dao.DataHistoryDao
import illyan.butler.data.local.room.dao.MessageDao
import illyan.butler.data.local.room.dao.ResourceDao
import illyan.butler.data.local.room.dao.UserDao
import illyan.butler.data.local.room.model.RoomAppSettings
import illyan.butler.data.local.room.model.RoomChat
import illyan.butler.data.local.room.model.RoomChatMember
import illyan.butler.data.local.room.model.RoomDataHistory
import illyan.butler.data.local.room.model.RoomMessage
import illyan.butler.data.local.room.model.RoomResource
import illyan.butler.data.local.room.model.RoomUser

@Suppress("NO_ACTUAL_FOR_EXPECT")
expect object ButlerDatabaseCtor : RoomDatabaseConstructor<ButlerDatabase>

@Database(
    entities = [
        RoomChat::class,
        RoomMessage::class,
        RoomDataHistory::class,
        RoomResource::class,
        RoomChatMember::class,
        RoomAppSettings::class,
        RoomUser::class
    ],
    version = 5
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
