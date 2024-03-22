package illyan.butler.data.sqldelight

import app.cash.sqldelight.async.coroutines.synchronous
import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.db.SqlSchema
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver

actual suspend fun provideSqlDriver(schema: SqlSchema<QueryResult.AsyncValue<Unit>>): SqlDriver {
    // TODO: Use a file-based database instead of in-memory for local persistence
    return JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        .also { schema.synchronous().create(it) }
}
