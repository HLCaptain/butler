package illyan.butler.data.sqldelight

import app.cash.sqldelight.Query
import illyan.butler.db.Database
import illyan.butler.di.KoinNames
import illyan.butler.di.flatMapLatestAsList
import illyan.butler.di.flatMapLatestAsOne
import illyan.butler.di.flatMapLatestAsOneOrNull
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single

// Modified from: https://github.com/cashapp/sqldelight/blob/master/sample-web/src/jsMain/kotlin/com/example/sqldelight/hockey/data/DbHelper.kt
@Single
class DatabaseHelper(
    private val databaseFlow: StateFlow<Database?>,
    @Named(KoinNames.DispatcherIO) private val dispatcherIO: CoroutineDispatcher,
) {
    private val mutex = Mutex()

    /**
     * Executes [block] with a [Database] instance.
     */
    suspend fun<T> withDatabase(block: suspend (Database) -> T) = mutex.withLock {
        block(databaseFlow.filterNotNull().first())
    }

    /**
     * When collected, executes [block] with a [Database] instance.
     */
    fun<T> withDatabaseResult(block: suspend (Database) -> T) = flow {
        mutex.withLock {
            emit(block(databaseFlow.filterNotNull().first()))
        }
    }.flowOn(dispatcherIO)

    fun<T : Any> queryAsOneFlow(block: suspend (Database) -> Query<T>) = withDatabaseResult(block).flatMapLatestAsOne()
    fun<T : Any> queryAsOneOrNullFlow(block: suspend (Database) -> Query<T>) = withDatabaseResult(block).flatMapLatestAsOneOrNull()
    fun<T : Any> queryAsListFlow(block: suspend (Database) -> Query<T>) = withDatabaseResult(block).flatMapLatestAsList()
}