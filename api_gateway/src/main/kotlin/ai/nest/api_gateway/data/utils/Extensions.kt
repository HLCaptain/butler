package ai.nest.api_gateway.data.utils

import ai.nest.api_gateway.data.model.identity.UserOptions
import ai.nest.api_gateway.data.model.localization.LabelDto
import ai.nest.api_gateway.di.apiHosts
import ai.nest.api_gateway.di.apiKeyToRequestFrom
import ai.nest.api_gateway.endpoints.utils.toListOfIntOrNull
import ai.nest.api_gateway.endpoints.utils.toListOfStringOrNull
import ai.nest.api_gateway.utils.APIs
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.websocket.receiveDeserialized
import io.ktor.client.plugins.websocket.sendSerialized
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.client.statement.HttpResponse
import io.ktor.http.Parameters
import io.ktor.http.isSuccess
import io.ktor.util.Attributes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Duration.Companion.days

suspend inline fun <reified T> HttpClient.tryToExecute(
    api: APIs,
    attributes: Attributes,
    noinline setErrorMessage: (errorCodes: List<String>) -> Flow<List<LabelDto>> = { flowOf(emptyList()) },
    method: HttpClient.() -> HttpResponse
): T {
    attributes.apiKeyToRequestFrom = api.key
    val response = method()
    if (response.status.isSuccess()) {
        return response.body<T>()
    } else {
//        val errorResponse = response.body<List<String>>()
//        val errorMessages = setErrorMessage(errorResponse).first()
//        throw LocalizedMessageException(errorMessages)
        throw response.getLocalizedException(setErrorMessage)
    }
}

suspend fun HttpResponse.getLocalizedException(
    getErrorMessages: (errorCodes: List<String>) -> Flow<List<LabelDto>>
): LocalizedMessageException {
    val errorResponse = body<List<String>>()
    val errorMessages = getErrorMessages(errorResponse).first()
    return LocalizedMessageException(errorMessages)
}

inline fun <reified T> HttpClient.tryToExecuteWebSocket(
    api: APIs,
    path: String,
    attributes: Attributes
): StateFlow<T?> {
    val stateFlow = MutableStateFlow<T?>(null)
    attributes.apiKeyToRequestFrom = api.key
    val host = attributes.apiHosts[api.key]
    launch(Dispatchers.IO) {
        webSocket(urlString = "ws://$host$path") {
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
    api: APIs,
    path: String,
    attributes: Attributes
) {
    attributes.apiKeyToRequestFrom = api.key
    val host = attributes.apiHosts[api.key]
    webSocket(urlString = "ws://$host$path") {
        try {
            sendSerialized(data)
        } catch (e: Exception) {
            throw Exception(e.message.toString())
        }
    }
}

suspend inline fun <reified T> HttpClient.tryToSendAndReceiveWebSocketData(
    data: T,
    api: APIs,
    path: String,
    attributes: Attributes
) = flow {
    attributes.apiKeyToRequestFrom = api.key
    val host = attributes.apiHosts[api.key]
    webSocket(urlString = "ws://$host$path") {
        sendSerialized(data)
        emit(receiveDeserialized<T>())
    }
}

fun Parameters.getUserOptions(): UserOptions {
    val page = this["page"]?.toIntOrNull()
    val limit = this["limit"]?.toIntOrNull()

    val query = this["query"]?.trim()
    val permissions = this["permissions"].toListOfIntOrNull()
    val countries = this["countries"].toListOfStringOrNull()
    return UserOptions(page, limit, query, permissions, countries)
}

// Utility function to get the date of the last month
fun getLastMonthDate() = Clock.System.now().minus(30.days).startOfDay()

fun getLastWeekDate() = Clock.System.now().minus(7.days).startOfDay()

// Extension function to convert a time instant to an ISO 8601 string
fun Instant.toIsoString() = toLocalDateTime(TimeZone.UTC).toString()

fun Instant.startOfDay() = toLocalDateTime(TimeZone.UTC).date.atStartOfDayIn(TimeZone.UTC)
