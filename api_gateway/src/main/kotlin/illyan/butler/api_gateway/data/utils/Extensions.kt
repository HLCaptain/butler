package illyan.butler.api_gateway.data.utils

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.websocket.DefaultClientWebSocketSession
import io.ktor.client.plugins.websocket.receiveDeserialized
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.client.statement.HttpResponse
import io.ktor.http.isSuccess
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Duration.Companion.days

suspend inline fun <reified T> HttpClient.tryToExecute(
    method: HttpClient.() -> HttpResponse
): T {
    return method().bodyOrThrow<T>()
}

suspend inline fun <reified T> HttpResponse.bodyOrThrow(): T {
    if (status.isSuccess()) {
        return body()
    } else {
        throw ApiException(status.value)
    }
}

inline fun <reified T> HttpClient.tryToExecuteWebSocket(
    path: String,
) = flow {
    webSocket(
        host = path.substringAfter("://").takeWhile { it != ':' },
        port = path.takeLastWhile { it != ':' }.takeWhile { it != '/' }.toInt(),
        path = path.takeLastWhile { it != ':' }.substringAfter("/")
    ) {
        incoming.receiveAsFlow().collect { emit(receiveDeserialized<T>()) }
    }
}

inline fun <reified T> DefaultClientWebSocketSession.incomingAsFlow() = incoming.receiveAsFlow().map { receiveDeserialized<T>() }

inline fun <K, reified V, F : Flow<V?>> MutableMap<K, F>.getOrPutWebSocketFlow(
    key: K,
    defaultValue: V? = null,
    createSession: () -> DefaultClientWebSocketSession
) = getOrPut(key) {
    val stateFlow = MutableStateFlow(defaultValue)
    createSession().incomingAsFlow<V>().map { data -> stateFlow.update { data } }
    stateFlow as F
}

// Utility function to get the date of the last month
fun getLastMonthDate() = Clock.System.now().minus(30.days).startOfDay()

fun getLastWeekDate() = Clock.System.now().minus(7.days).startOfDay()

// Extension function to convert a time instant to an ISO 8601 string
fun Instant.toIsoString() = toLocalDateTime(TimeZone.UTC).toString()

fun Instant.startOfDay() = toLocalDateTime(TimeZone.UTC).date.atStartOfDayIn(TimeZone.UTC)
