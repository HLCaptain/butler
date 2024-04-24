package illyan.butler.data.sqldelight

import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.db.SqlSchema
import illyan.butler.db.Chat
import illyan.butler.db.Database
import illyan.butler.db.ErrorEvent
import illyan.butler.di.KoinNames
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single

expect suspend fun provideSqlDriver(schema: SqlSchema<QueryResult.AsyncValue<Unit>>): SqlDriver

private suspend fun createDatabase(): Database {
    val driver = provideSqlDriver(Database.Schema)

    val database = Database(
        driver = driver,
        ChatAdapter = Chat.Adapter(
            membersAdapter = listAdapter,
            aiEndpointsAdapter = mapAdapter
        ),
        ErrorEventAdapter = ErrorEvent.Adapter(
            stateAdapter = errorStateAdapter,
            metadataAdapter = mapAdapter
        )
    )

    Napier.d("Database created")
    return database
}

@Single
fun provideDatabaseFlow(
    @Named(KoinNames.CoroutineScopeIOWithoutHandler) coroutineScopeIO: CoroutineScope
): StateFlow<Database?> {
    val stateFlow = MutableStateFlow<Database?>(null)
    coroutineScopeIO.launch {
        createDatabase().apply { stateFlow.update { this } }
    }
    return stateFlow
}
