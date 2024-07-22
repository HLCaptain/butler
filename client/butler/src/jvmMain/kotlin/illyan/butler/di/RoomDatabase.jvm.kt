package illyan.butler.di

import androidx.room.Room
import androidx.room.RoomDatabase
import illyan.butler.config.BuildConfig
import illyan.butler.data.room.ButlerDatabase
import org.koin.core.annotation.Single
import java.io.File

@Single
actual fun getRoomDatabaseBuilder(): RoomDatabase.Builder<ButlerDatabase> {
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
