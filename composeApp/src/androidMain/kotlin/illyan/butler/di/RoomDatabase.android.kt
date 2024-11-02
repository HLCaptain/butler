package illyan.butler.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import illyan.butler.config.BuildConfig
import illyan.butler.data.local.room.ButlerDatabase
import org.koin.core.context.GlobalContext

actual fun getPlatformRoomDatabaseBuilder(): RoomDatabase.Builder<ButlerDatabase> {
    val context = GlobalContext.get().get<Context>()
    val appContext = context.applicationContext
    val dbFile = appContext.getDatabasePath("butler.db")
    if (BuildConfig.RESET_ROOM_DB) {
        dbFile.delete()
        dbFile.createNewFile()
    }
    return Room.databaseBuilder<ButlerDatabase>(
        context = appContext,
        name = dbFile.absolutePath
    )
}
