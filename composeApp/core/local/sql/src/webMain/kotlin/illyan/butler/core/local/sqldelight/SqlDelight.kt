package illyan.butler.core.local.sqldelight

import app.cash.sqldelight.async.coroutines.awaitCreate
import app.cash.sqldelight.driver.worker.createDefaultWebWorkerDriver
import illyan.butler.core.local.sqldelight.db.ButlerDatabase
import illyan.butler.di.KoinNames
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single

@Single
fun createDatabase(
    @Named(KoinNames.CoroutineScopeIO) coroutineScope: CoroutineScope
): ButlerDatabase {
    val driver = createDefaultWebWorkerDriver()
    return ButlerDatabase(driver = driver).also {
        coroutineScope.launch {
            ButlerDatabase.Schema.awaitCreate(driver)
        }
    }
}

@Single
fun createDatabaseHelper(database: ButlerDatabase) = DatabaseHelper(database, ButlerDatabase.Schema, createDefaultWebWorkerDriver())
