package illyan.butler.api_gateway.data.service

import ai.nest.api_gateway.data.model.chat.ChatDto
import ai.nest.api_gateway.data.model.chat.MessageDto
import ai.nest.api_gateway.data.model.response.PaginationResponse
import ai.nest.api_gateway.data.utils.bodyOrThrow
import ai.nest.api_gateway.data.utils.getLastMonthDate
import ai.nest.api_gateway.data.utils.getLastWeekDate
import ai.nest.api_gateway.data.utils.tryToExecuteWebSocket
import ai.nest.api_gateway.utils.AppConfig
import io.ktor.client.HttpClient
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import kotlinx.datetime.Clock
import org.koin.core.annotation.Single

@Single
class ChatService(private val client: HttpClient) {
    fun receiveMessages(userId: String, chatId: String) = client.tryToExecuteWebSocket<MessageDto>("${AppConfig.Api.CHAT_API_URL}/$userId/chats/$chatId")
    suspend fun sendMessage(userId: String, message: MessageDto) = client.post("${AppConfig.Api.CHAT_API_URL}/$userId/chats/${message.chatId}/messages") { setBody(message) }
    suspend fun editMessage(userId: String, message: MessageDto) = client.put("${AppConfig.Api.CHAT_API_URL}/$userId/chats/${message.chatId}/messages/${message.id}") { setBody(message) }
    suspend fun deleteMessage(userId: String, chatId: String, messageId: String) = client.delete("${AppConfig.Api.CHAT_API_URL}/$userId/chats/$chatId/messages/$messageId").bodyOrThrow<Boolean>()

    fun receiveChats(userId: String) = client.tryToExecuteWebSocket<MessageDto>("${AppConfig.Api.CHAT_API_URL}/$userId/chats")
    suspend fun getChat(userId: String, chatId: String) = client.get("${AppConfig.Api.CHAT_API_URL}/$userId/chats/$chatId").bodyOrThrow<ChatDto>()
    suspend fun createChat(userId: String, chat: ChatDto) = client.post("${AppConfig.Api.CHAT_API_URL}/$userId/chats") { setBody(chat) }.bodyOrThrow<ChatDto>()
    suspend fun editChat(userId: String, chat: ChatDto) = client.put("${AppConfig.Api.CHAT_API_URL}/$userId/chats/${chat.id}") { setBody(chat) }
    suspend fun deleteChat(userId: String, chatId: String) = client.delete("${AppConfig.Api.CHAT_API_URL}/$userId/chats/$chatId").bodyOrThrow<Boolean>()

    /**
     * @param fromDate epoch milli
     * @param toDate epoch milli
     * @return list of [ChatDto]
     */
    private suspend fun getChats(
        userId: String,
        fromDate: Long,
        toDate: Long = Clock.System.now().toEpochMilliseconds()
    ) = client.get("${AppConfig.Api.CHAT_API_URL}/$userId/chats") {
        parameter("from", fromDate)
        parameter("to", toDate)
    }.bodyOrThrow<PaginationResponse<ChatDto>>()

    suspend fun getChatsLastMonth(userId: String) = getChats(
        userId = userId,
        fromDate = getLastMonthDate().toEpochMilliseconds()
    )

    suspend fun getChatsLastWeek(userId: String) = getChats(
        userId = userId,
        fromDate = getLastWeekDate().toEpochMilliseconds()
    )

    suspend fun getChats(
        userId: String,
        limit: Int,
        offset: Int
    ) = client.get("${AppConfig.Api.CHAT_API_URL}/$userId/chats") {
        parameter("limit", limit)
        parameter("offset", offset)
    }.bodyOrThrow<PaginationResponse<ChatDto>>()

    suspend fun getPreviousChats(
        userId: String,
        limit: Int,
        timestamp: Long
    ) = client.get("${AppConfig.Api.CHAT_API_URL}/$userId/chats") {
        parameter("limit", limit)
        parameter("timestamp", timestamp)
    }.bodyOrThrow<PaginationResponse<ChatDto>>()

    suspend fun getPreviousChats(
        userId: String,
        limit: Int,
        offset: Int
    ) = client.get("${AppConfig.Api.CHAT_API_URL}/$userId/chats") {
        parameter("limit", limit)
        parameter("offset", offset)
    }.bodyOrThrow<PaginationResponse<ChatDto>>()

    suspend fun getPreviousMessages(
        userId: String,
        chatId: String,
        limit: Int,
        timestamp: Long
    ) = client.get("${AppConfig.Api.CHAT_API_URL}/$userId/chats/$chatId") {
        parameter("limit", limit)
        parameter("timestamp", timestamp)
    }.bodyOrThrow<PaginationResponse<MessageDto>>()

    suspend fun getMessages(
        userId: String,
        chatId: String,
        limit: Int,
        offset: Int
    ) = client.get("${AppConfig.Api.CHAT_API_URL}/$userId/chats/$chatId") {
        parameter("offset", offset)
        parameter("limit", limit)
    }.bodyOrThrow<PaginationResponse<MessageDto>>()
}
