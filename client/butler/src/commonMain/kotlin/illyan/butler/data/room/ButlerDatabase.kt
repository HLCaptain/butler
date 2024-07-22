package illyan.butler.data.room

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import illyan.butler.data.room.dao.AppSettingsDao
import illyan.butler.data.room.dao.ChatDao
import illyan.butler.data.room.dao.ChatMemberDao
import illyan.butler.data.room.dao.DataHistoryDao
import illyan.butler.data.room.dao.MessageDao
import illyan.butler.data.room.dao.ResourceDao
import illyan.butler.data.room.dao.UserDao
import illyan.butler.data.room.model.RoomAppSettings
import illyan.butler.data.room.model.RoomChat
import illyan.butler.data.room.model.RoomChatMember
import illyan.butler.data.room.model.RoomDataHistory
import illyan.butler.data.room.model.RoomMessage
import illyan.butler.data.room.model.RoomResource
import illyan.butler.data.room.model.RoomUser

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
