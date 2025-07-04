package illyan.butler.core.local.room

import androidx.room.Room
import androidx.room.RoomDatabase
import illyan.butler.config.BuildConfig
import java.io.File

actual fun getPlatformRoomDatabaseBuilder(): RoomDatabase.Builder<ButlerDatabase> {
    val dbFile = if (BuildConfig.DEBUG) {
        File(System.getProperty("user.home"), "butler.db")
    } else {
        File(System.getProperty("java.io.tmpdir"), "butler.db")
    }
    if (BuildConfig.RESET_ROOM_DB) {
        dbFile.delete()
        dbFile.createNewFile()
    }
    return Room.databaseBuilder<ButlerDatabase>(
        name = dbFile.absolutePath,
    )
}
