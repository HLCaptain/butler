package illyan.butler.data.sqldelight

import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.db.SqlSchema
import illyan.butler.db.Database
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.annotation.Single

expect suspend fun provideSqlDriver(schema: SqlSchema<QueryResult.AsyncValue<Unit>>): SqlDriver

private suspend fun createDatabase(): Database {
    val driver = provideSqlDriver(Database.Schema)

    val database = Database(driver)

    Napier.d("Database created")
    return database
}

@Single
fun provideDatabaseFlow(coroutineScopeIO: CoroutineScope): StateFlow<Database?> {
    val stateFlow = MutableStateFlow<Database?>(null)
    coroutineScopeIO.launch {
        createDatabase().apply { stateFlow.update { this } }
    }
    return stateFlow
}
