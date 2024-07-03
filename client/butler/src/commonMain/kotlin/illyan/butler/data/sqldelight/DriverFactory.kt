package illyan.butler.data.sqldelight

import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.db.SqlSchema
import illyan.butler.db.Chat
import illyan.butler.db.Database
import illyan.butler.db.ErrorEvent
import illyan.butler.db.Message
import illyan.butler.db.Resource
import io.github.aakira.napier.Napier

expect suspend fun provideSqlDriver(schema: SqlSchema<QueryResult.AsyncValue<Unit>>): SqlDriver

suspend fun createDatabase(): Database {
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
        ),
        ResourceAdapter = Resource.Adapter(
            data_Adapter = byteArrayToTextAdapter
        ),
        MessageAdapter = Message.Adapter(
            resourceIdsAdapter = listAdapter
        )
    )

    Napier.d("Database created")
    return database
}
