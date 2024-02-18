package ai.nest.api_gateway.data.service

import ai.nest.api_gateway.data.model.chat.ChatDto
import ai.nest.api_gateway.data.model.chat.MessageDto
import ai.nest.api_gateway.data.model.response.PaginationResponse
import ai.nest.api_gateway.data.utils.bodyOrThrow
import ai.nest.api_gateway.data.utils.getLastMonthDate
import ai.nest.api_gateway.data.utils.getLastWeekDate
import ai.nest.api_gateway.data.utils.tryToExecuteWebSocket
import ai.nest.api_gateway.data.utils.tryToSendAndReceiveWebSocketData
import ai.nest.api_gateway.data.utils.tryToSendWebSocketData
import ai.nest.api_gateway.utils.AppConfig
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import kotlinx.datetime.Clock
import org.koin.core.annotation.Single

@Single
class ChatService(private val client: HttpClient) {
    fun receiveChatMessages(chatId: String) = client.tryToExecuteWebSocket<MessageDto>("${AppConfig.Api.CHAT_API_URL}/chat/$chatId")

    suspend fun sendChatMessage(
        message: MessageDto,
        chatId: String
    ) = client.tryToSendWebSocketData(
        data = message,
        path = "${AppConfig.Api.CHAT_API_URL}/chat/$chatId"
    )

    suspend fun sendAndReceiveMessage(
        message: MessageDto,
        chatId: String
    ) = client.tryToSendAndReceiveWebSocketData(
        data = message,
        path = "${AppConfig.Api.CHAT_API_URL}/chat/$chatId"
    )

    /**
     * @param fromDate epoch milli
     * @param toDate epoch milli
     * @return list of [ChatDto]
     */
    suspend fun getUserChatHistoryByDate(
        userId: String,
        fromDate: Long,
        toDate: Long = Clock.System.now().toEpochMilliseconds(),
    ) = client.get("${AppConfig.Api.CHAT_API_URL}/chat/history/$userId") {
        parameter("fromDate", fromDate)
        parameter("toDate", toDate)
    }.bodyOrThrow<List<ChatDto>>()

    suspend fun getUserChatHistoryLastMonth(
        userId: String,
    ) = getUserChatHistoryByDate(
        userId = userId,
        fromDate = getLastMonthDate().toEpochMilliseconds(),
    )

    suspend fun getUserChatHistoryLastWeek(
        userId: String,
    ) = getUserChatHistoryByDate(
        userId = userId,
        fromDate = getLastWeekDate().toEpochMilliseconds(),
    )

    suspend fun getUserChatHistory(
        userId: String,
        page: Int,
        limit: Int,
    ) = client.get("${AppConfig.Api.CHAT_API_URL}/chat/history/$userId") {
        parameter("page", page)
        parameter("limit", limit)
    }.bodyOrThrow<PaginationResponse<ChatDto>>()

    suspend fun getPreviousChatMessages(
        chatId: String,
        untilDate: Long,
        limit: Int,
    ) = client.get("${AppConfig.Api.CHAT_API_URL}/chat/$chatId") {
        parameter("untilDate", untilDate)
        parameter("limit", limit)
    }.bodyOrThrow<List<MessageDto>>()
}
