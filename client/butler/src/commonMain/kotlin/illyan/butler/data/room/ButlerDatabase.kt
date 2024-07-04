package illyan.butler.data.room

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import illyan.butler.data.room.dao.ChatDao
import illyan.butler.data.room.dao.DataHistoryDao
import illyan.butler.data.room.dao.MessageDao
import illyan.butler.data.room.model.RoomChat
import illyan.butler.data.room.model.RoomDataHistory
import illyan.butler.data.room.model.RoomMessage

@Database(
    entities = [
        RoomChat::class,
        RoomMessage::class,
        RoomDataHistory::class
    ],
    version = 1
)
@TypeConverters(Converters::class)
abstract class ButlerDatabase : RoomDatabase() {
    abstract fun chatDao(): ChatDao
    abstract fun messageDao(): MessageDao
    abstract fun dataHistoryDao(): DataHistoryDao
}