package ai.nest.api_gateway.data.service

import ai.nest.api_gateway.data.model.chat.ChatDto
import ai.nest.api_gateway.data.model.chat.MessageDto
import ai.nest.api_gateway.data.model.chat.TicketDto
import ai.nest.api_gateway.data.model.response.PaginationResponse
import ai.nest.api_gateway.data.utils.getLastMonthDate
import ai.nest.api_gateway.data.utils.getLastWeekDate
import ai.nest.api_gateway.data.utils.tryToExecute
import ai.nest.api_gateway.data.utils.tryToExecuteWebSocket
import ai.nest.api_gateway.data.utils.tryToSendAndReceiveWebSocketData
import ai.nest.api_gateway.data.utils.tryToSendWebSocketData
import ai.nest.api_gateway.utils.APIs
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.util.Attributes
import java.util.Locale
import kotlinx.datetime.Clock
import org.koin.core.annotation.Single

@Single
class ChatService(
    private val client: HttpClient,
    private val attributes: Attributes,
    private val localizationService: LocalizationService
) {
    suspend fun createTicket(
        ticket: TicketDto,
        locale: Locale
    ) = client.tryToExecute<TicketDto>(
        api = APIs.CHAT_API,
        attributes = attributes,
        setErrorMessage = { localizationService.getLocalizedMessages(it, locale) }
    ) {
        post("/chat/ticket") {
            setBody(ticket)
        }
    }

    suspend fun updateTicketState(
        ticketId: String,
        state: Boolean,
        locale: Locale
    ) = client.tryToExecute<Boolean>(
        api = APIs.CHAT_API,
        attributes = attributes,
        setErrorMessage = { localizationService.getLocalizedMessages(it, locale) }
    ) {
        put("/chat/$ticketId") {
            parameter("state", state)
        }
    }

    suspend fun receiveTicket(supportId: String) = client.tryToExecuteWebSocket<TicketDto>(
        api = APIs.CHAT_API,
        attributes = attributes,
        path = "/chat/tickets/$supportId"
    )

    suspend fun receiveChatMessages(chatId: String) = client.tryToExecuteWebSocket<MessageDto>(
        api = APIs.CHAT_API,
        attributes = attributes,
        path = "/chat/$chatId"
    )

    suspend fun sendChatMessage(
        message: MessageDto,
        chatId: String
    ) = client.tryToSendWebSocketData(
        data = message,
        api = APIs.CHAT_API,
        attributes = attributes,
        path = "/chat/$chatId"
    )

    suspend fun sendAndReceiveMessage(
        message: MessageDto,
        chatId: String
    ) = client.tryToSendAndReceiveWebSocketData(
        data = message,
        api = APIs.CHAT_API,
        attributes = attributes,
        path = "/chat/$chatId"
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
        locale: Locale
    ) = client.tryToExecute<List<ChatDto>>(
        APIs.CHAT_API,
        attributes = attributes,
        setErrorMessage = { localizationService.getLocalizedMessages(it, locale) }
    ) {
        get("/chat/history/$userId") {
            parameter("fromDate", fromDate)
            parameter("toDate", toDate)
        }
    }

    suspend fun getUserChatHistoryLastMonth(
        userId: String,
        locale: Locale
    ) = getUserChatHistoryByDate(
        userId = userId,
        fromDate = getLastMonthDate().toEpochMilliseconds(),
        locale = locale
    )

    suspend fun getUserChatHistoryLastWeek(
        userId: String,
        locale: Locale
    ) = getUserChatHistoryByDate(
        userId = userId,
        fromDate = getLastWeekDate().toEpochMilliseconds(),
        locale = locale
    )

    suspend fun getUserChatHistory(
        userId: String,
        page: Int,
        limit: Int,
        locale: Locale
    ) = client.tryToExecute<PaginationResponse<ChatDto>>(
        APIs.CHAT_API,
        attributes = attributes,
        setErrorMessage = { localizationService.getLocalizedMessages(it, locale) }
    ) {
        get("/chat/history/$userId") {
            parameter("page", page)
            parameter("limit", limit)
        }
    }

    suspend fun getPreviousChatMessages(
        chatId: String,
        untilDate: Long,
        limit: Int,
        locale: Locale
    ) = client.tryToExecute<List<MessageDto>>(
        APIs.CHAT_API,
        attributes = attributes,
        setErrorMessage = { localizationService.getLocalizedMessages(it, locale) }
    ) {
        get("/chat/$chatId") {
            parameter("untilDate", untilDate)
            parameter("limit", limit)
        }
    }
}
