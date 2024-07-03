package illyan.butler.data.sqldelight

import app.cash.sqldelight.Query
import illyan.butler.db.Database
import illyan.butler.utils.flatMapLatestAsList
import illyan.butler.utils.flatMapLatestAsOne
import illyan.butler.utils.flatMapLatestAsOneOrNull
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.koin.core.annotation.Single

// Modified from: https://github.com/cashapp/sqldelight/blob/master/sample-web/src/jsMain/kotlin/com/example/sqldelight/hockey/data/DbHelper.kt
@Single
class DatabaseHelper {
    private val mutex = Mutex()

    private var database: Database? = null
    private suspend fun getDatabase() = database ?: createDatabase().apply { database = this }

    /**
     * Executes [block] with a [Database] instance.
     */
    suspend fun<T> withDatabase(block: suspend (Database) -> T) = mutex.withLock { block(getDatabase()) }

    /**
     * When collected, executes [block] with a [Database] instance.
     */
    fun<T> withDatabaseResult(block: suspend (Database) -> T) = flow {
        mutex.withLock { emit(block(getDatabase())) }
    }.flowOn(Dispatchers.IO)

    fun<T : Any> queryAsOneFlow(block: suspend (Database) -> Query<T>) = withDatabaseResult(block).flatMapLatestAsOne()
    fun<T : Any> queryAsOneOrNullFlow(block: suspend (Database) -> Query<T>) = withDatabaseResult(block).flatMapLatestAsOneOrNull()
    fun<T : Any> queryAsListFlow(block: suspend (Database) -> Query<T>) = withDatabaseResult(block).flatMapLatestAsList()
}