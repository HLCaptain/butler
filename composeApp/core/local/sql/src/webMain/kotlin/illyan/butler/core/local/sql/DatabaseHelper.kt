package illyan.butler.core.local.sqldelight

import app.cash.sqldelight.Query
import app.cash.sqldelight.SuspendingTransacter
import app.cash.sqldelight.async.coroutines.awaitAsList
import app.cash.sqldelight.async.coroutines.awaitAsOne
import app.cash.sqldelight.async.coroutines.awaitAsOneOrNull
import app.cash.sqldelight.async.coroutines.awaitCreate
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.db.SqlSchema
import app.cash.sqldelight.driver.worker.WebWorkerException
import io.github.aakira.napier.Napier
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.koin.core.annotation.Single

// Modified from: https://github.com/cashapp/sqldelight/blob/master/sample-web/src/jsMain/kotlin/com/example/sqldelight/hockey/data/DbHelper.kt
@Single
class DatabaseHelper<D : SuspendingTransacter>(
    private val database: D,
    private val schema: SqlSchema<QueryResult.AsyncValue<Unit>>,
    private val driver: SqlDriver
) {
    private val mutex = Mutex()

    private var isDatabaseCreated = false

    /**
     * Executes [block] with a [D] instance.
     */
    suspend fun<T> withDatabase(block: suspend D.() -> T) = mutex.withLock {
        if (!isDatabaseCreated) {
            Napier.v { "Creating database..." }
            try {
                schema.awaitCreate(driver)
            } catch (e: WebWorkerException) {
                Napier.e(e) { "WebWorkerException occurred while creating the database: ${e.message}" }
            } catch (e: Exception) {
                Napier.e(e) { "An error occurred while creating the database: ${e.message}" }
            }
            isDatabaseCreated = true
            Napier.v { "Database created successfully." }
        }
        var remainingAttempts = 2
        while (remainingAttempts > 0) {
            try {
                Napier.v { "Attempting to access the database..." }
                return@withLock block(database).also {
                    Napier.v { "Database access successful." }
                }
            } catch (e: WebWorkerException) {
                Napier.e(e) { "WebWorkerException occurred while accessing the database: ${e.message}" }
                Napier.v { "Attempting to recreate the database..." }
                schema.awaitCreate(driver)
                remainingAttempts--
            } catch (e: Exception) {
                Napier.e(e) { "An error occurred while accessing the database: ${e.message}" }
            }
        }
        Napier.e { "Failed to access the database after multiple attempts." }
        null
    }

    /**d
     * When collected, executes [block] with a [D] instance.
     */
    fun<T> withDatabaseResult(block: suspend D.() -> T) = flow {
        mutex.withLock {
            if (!isDatabaseCreated) {
                Napier.v { "Creating database..." }
                try {
                    schema.awaitCreate(driver)
                } catch (e: WebWorkerException) {
                    Napier.e(e) { "WebWorkerException occurred while creating the database: ${e.message}" }
                } catch (e: Exception) {
                    Napier.e(e) { "An error occurred while creating the database: ${e.message}" }
                }
                isDatabaseCreated = true
                Napier.v { "Database created successfully." }
            }
            var remainingAttempts = 2
            while (remainingAttempts > 0) {
                try {
                    Napier.v { "Attempting to access the database..." }
                    val result = block(database)
                    Napier.v { "Database access successful." }
                    emit(result)
                    return@flow
                } catch (e: WebWorkerException) {
                    Napier.e(e) { "WebWorkerException occurred while accessing the database: ${e.message}" }
                    Napier.v { "Attempting to recreate the database..." }
                    schema.awaitCreate(driver)
                    remainingAttempts--
                } catch (e: Exception) {
                    Napier.e(e) { "An error occurred while accessing the database: ${e.message}" }
                }
            }
        }
    }

    fun<T : Any> queryAsOneFlow(block: suspend D.() -> Query<T>) = withDatabaseResult(block).flatMapLatestAsOne()
    fun<T : Any> queryAsOneOrNullFlow(block: suspend D.() -> Query<T>) = withDatabaseResult(block).flatMapLatestAsOneOrNull()
    fun<T : Any> queryAsListFlow(block: suspend D.() -> Query<T>) = withDatabaseResult(block).flatMapLatestAsList()
}

@OptIn(ExperimentalCoroutinesApi::class)
fun <T : Any, R : Any?> Flow<Query<T>>.flatMapLatestAs(map: suspend (Query<T>) -> R): Flow<R> =
    flatMapLatest { query ->
        query.asFlow().map(map)
    }

fun <T : Any, R : Any?> Flow<Query<T>>.flatMapLatestAsOne(map: suspend (T) -> R): Flow<R> = flatMapLatestAs(Query<T>::awaitAsOne).map(map)
fun <T : Any, R : Any?> Flow<Query<T>>.flatMapLatestAsOneOrNull(map: suspend (T?) -> R): Flow<R> = flatMapLatestAs(Query<T>::awaitAsOneOrNull).map(map)
fun <T : Any, R : Any?> Flow<Query<T>>.flatMapLatestAsList(map: suspend (List<T>) -> R): Flow<R> = flatMapLatestAs(Query<T>::awaitAsList).map(map)

fun <T : Any> Flow<Query<T>>.flatMapLatestAsOne(): Flow<T> = flatMapLatestAs(Query<T>::awaitAsOne)
fun <T : Any> Flow<Query<T>>.flatMapLatestAsOneOrNull(): Flow<T?> = flatMapLatestAs(Query<T>::awaitAsOneOrNull)
fun <T : Any> Flow<Query<T>>.flatMapLatestAsList(): Flow<List<T>> = flatMapLatestAs(Query<T>::awaitAsList)