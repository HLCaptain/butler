package illyan.butler.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import illyan.butler.data.room.ButlerDatabase
import org.koin.core.annotation.Single
import org.koin.core.context.GlobalContext

@Single
actual fun getRoomDatabaseBuilder(): RoomDatabase.Builder<ButlerDatabase> {
    val context = GlobalContext.get().get<Context>()
    val appContext = context.applicationContext
    val dbFile = appContext.getDatabasePath("butler.db")
    return Room.databaseBuilder<ButlerDatabase>(
        context = appContext,
        name = dbFile.absolutePath
    )
}
