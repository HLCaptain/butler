package illyan.butler.di

import androidx.room.Room
import androidx.room.RoomDatabase
import illyan.butler.data.room.ButlerDatabase
import org.koin.core.annotation.Single
import java.io.File

@Single
actual fun getRoomDatabaseBuilder(): RoomDatabase.Builder<ButlerDatabase> {
    val dbFile = File(System.getProperty("java.io.tmpdir"), "butler.db")
    return Room.databaseBuilder<ButlerDatabase>(
        name = dbFile.absolutePath,
    )
}
