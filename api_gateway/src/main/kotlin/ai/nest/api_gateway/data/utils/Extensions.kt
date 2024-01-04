package ai.nest.api_gateway.data.utils

import ai.nest.api_gateway.data.model.identity.UserOptions
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
import io.ktor.util.AttributeKey
import io.ktor.util.Attributes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

suspend inline fun <reified T> HttpClient.tryToExecute(
    api: APIs,
    attributes: Attributes,
    setErrorMessage: (errorCodes: List<Int>) -> Map<Int, String> = { emptyMap() },
    method: HttpClient.() -> HttpResponse
): T {
    attributes.put(AttributeKey("API"), api.value)
    val response = this.method()
    if (response.status.isSuccess()) {
        return response.body<T>()
    } else {
        val errorResponse = response.body<List<Int>>()
        val errorMessage = setErrorMessage(errorResponse)
        throw LocalizedMessageException(errorMessage)
    }
}

suspend inline fun <reified T> HttpClient.tryToExecuteWebSocket(
    api: APIs,
    path: String,
    attributes: Attributes
): Flow<T> {
    attributes.put(AttributeKey("API"), api.value)
    val host = System.getenv(attributes[AttributeKey("API")])
    return flow {
        webSocket(urlString = "ws://$host$path") {
            while (true) {
                try {
                    emit(receiveDeserialized<T>())
                } catch (e: Exception) {
                    throw Exception(e.message.toString())
                }
            }
        }
    }.flowOn(Dispatchers.IO)
}

suspend inline fun <reified T> HttpClient.tryToSendWebSocketData(
    data: T,
    api: APIs,
    path: String,
    attributes: Attributes
) {
    attributes.put(AttributeKey("API"), api.value)
    val host = System.getenv(attributes[AttributeKey("API")])
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
): Flow<T> {
    attributes.put(AttributeKey("API"), api.value)
    val host = System.getenv(attributes[AttributeKey("API")])
    return flow {
        webSocket(urlString = "ws://$host$path") {
            sendSerialized(data)
            emit(receiveDeserialized<T>())
        }
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