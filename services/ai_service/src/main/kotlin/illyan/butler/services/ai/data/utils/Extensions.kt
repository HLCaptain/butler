package illyan.butler.services.ai.data.utils

import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.receiveDeserialized
import io.ktor.client.plugins.websocket.webSocket
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Duration.Companion.days

// Utility function to get the date of the last month
fun getLastMonthDate() = Clock.System.now().minus(30.days).startOfDay()

fun getLastWeekDate() = Clock.System.now().minus(7.days).startOfDay()

// Extension function to convert a time instant to an ISO 8601 string
fun Instant.toIsoString() = toLocalDateTime(TimeZone.UTC).toString()

fun Instant.startOfDay() = toLocalDateTime(TimeZone.UTC).date.atStartOfDayIn(TimeZone.UTC)

inline fun <reified T> HttpClient.tryToExecuteWebSocket(path: String): StateFlow<T?> {
    val stateFlow = MutableStateFlow<T?>(null)
    launch(Dispatchers.IO) {
        webSocket(urlString = "ws://$path") {
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
