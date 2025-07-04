package illyan.butler.core.local.sqldelight

import app.cash.sqldelight.driver.worker.createDefaultWebWorkerDriver
import illyan.butler.core.local.sqldelight.db.ButlerDatabase

fun createDatabase(): ButlerDatabase {
    return ButlerDatabase(createDefaultWebWorkerDriver())
}
