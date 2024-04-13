package illyan.butler.api_gateway.data.utils

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.websocket.receiveDeserialized
import io.ktor.client.plugins.websocket.sendSerialized
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.client.statement.HttpResponse
import io.ktor.http.isSuccess
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
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
): StateFlow<T?> {
    val stateFlow = MutableStateFlow<T?>(null)
    launch(Dispatchers.IO) {
        webSocket(urlString = path.replaceFirst("http", "ws")) {
            while (true) {
                try {
                    stateFlow.update { receiveDeserialized<T>() }
                } catch (e: Exception) {
                    throw Exception(e.message.toString())
                }
            }
        }
    }
    return stateFlow.asStateFlow()
}

suspend inline fun <reified T> HttpClient.tryToSendWebSocketData(
    data: T,
    path: String
) {
    webSocket(urlString = path.replaceFirst("http", "ws")) {
        try {
            sendSerialized(data)
        } catch (e: Exception) {
            throw Exception(e.message.toString())
        }
    }
}

suspend inline fun <reified T> HttpClient.tryToSendAndReceiveWebSocketData(
    data: T,
    path: String
) = flow {
    webSocket(urlString = path.replaceFirst("http", "ws")) {
        sendSerialized(data)
        emit(receiveDeserialized<T>())
    }
}

// Utility function to get the date of the last month
fun getLastMonthDate() = Clock.System.now().minus(30.days).startOfDay()

fun getLastWeekDate() = Clock.System.now().minus(7.days).startOfDay()

// Extension function to convert a time instant to an ISO 8601 string
fun Instant.toIsoString() = toLocalDateTime(TimeZone.UTC).toString()

fun Instant.startOfDay() = toLocalDateTime(TimeZone.UTC).date.atStartOfDayIn(TimeZone.UTC)
