package illyan.butler.api_gateway.data.service

import illyan.butler.api_gateway.data.model.chat.ChatDto
import illyan.butler.api_gateway.data.model.chat.MessageDto
import illyan.butler.api_gateway.data.model.response.PaginationResponse
import illyan.butler.api_gateway.data.utils.getLastMonthDate
import illyan.butler.api_gateway.data.utils.getLastWeekDate
import illyan.butler.api_gateway.data.utils.tryToExecuteWebSocket
import illyan.butler.api_gateway.utils.AppConfig
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.encodeURLPath
import kotlinx.datetime.Clock
import org.koin.core.annotation.Single

@Single
class ChatService(private val client: HttpClient) {
    fun receiveMessages(userId: String, chatId: String) = client.tryToExecuteWebSocket<List<MessageDto>>("${AppConfig.Api.CHAT_API_URL}/${userId.encodeURLPath()}/chats/$chatId")
    suspend fun sendMessage(userId: String, message: MessageDto) = client.post("${AppConfig.Api.CHAT_API_URL}/${userId.encodeURLPath()}/chats/${message.chatId}/messages") { setBody(message) }.body<MessageDto>()
    suspend fun editMessage(userId: String, messageId: String, message: MessageDto) = client.put("${AppConfig.Api.CHAT_API_URL}/${userId.encodeURLPath()}/chats/${message.chatId}/messages/$messageId") { setBody(message) }.body<MessageDto>()
    suspend fun deleteMessage(userId: String, chatId: String, messageId: String) = client.delete("${AppConfig.Api.CHAT_API_URL}/${userId.encodeURLPath()}/chats/$chatId/messages/$messageId").body<Boolean>()

    fun receiveChats(userId: String) = client.tryToExecuteWebSocket<MessageDto>("${AppConfig.Api.CHAT_API_URL}/${userId.encodeURLPath()}/chats")
    suspend fun getChat(userId: String, chatId: String) = client.get("${AppConfig.Api.CHAT_API_URL}/${userId.encodeURLPath()}/chats/$chatId").body<ChatDto>()
    suspend fun createChat(userId: String, chat: ChatDto) = client.post("${AppConfig.Api.CHAT_API_URL}/${userId.encodeURLPath()}/chats") { setBody(chat) }.body<ChatDto>()
    suspend fun editChat(userId: String, chatId: String, chat: ChatDto) = client.put("${AppConfig.Api.CHAT_API_URL}/${userId.encodeURLPath()}/chats/$chatId") { setBody(chat) }.body<ChatDto>()
    suspend fun deleteChat(userId: String, chatId: String) = client.delete("${AppConfig.Api.CHAT_API_URL}/${userId.encodeURLPath()}/chats/$chatId").body<Boolean>()

    /**
     * @param fromDate epoch milli
     * @param toDate epoch milli
     * @return list of [ChatDto]
     */
    private suspend fun getChats(
        userId: String,
        fromDate: Long,
        toDate: Long = Clock.System.now().toEpochMilliseconds()
    ) = client.get("${AppConfig.Api.CHAT_API_URL}/${userId.encodeURLPath()}/chats") {
        parameter("from", fromDate)
        parameter("to", toDate)
    }.body<PaginationResponse<ChatDto>>()

    suspend fun getChatsLastMonth(userId: String) = getChats(
        userId = userId,
        fromDate = getLastMonthDate().toEpochMilliseconds()
    )

    suspend fun getChatsLastWeek(userId: String) = getChats(
        userId = userId,
        fromDate = getLastWeekDate().toEpochMilliseconds()
    )

    suspend fun getChats(userId: String) = client.get("${AppConfig.Api.CHAT_API_URL}/${userId.encodeURLPath()}/chats").body<List<ChatDto>>()

    suspend fun getChats(
        userId: String,
        limit: Int,
        offset: Int
    ) = client.get("${AppConfig.Api.CHAT_API_URL}/${userId.encodeURLPath()}/chats") {
        parameter("limit", limit)
        parameter("offset", offset)
    }.body<List<ChatDto>>()

    suspend fun getPreviousChats(
        userId: String,
        limit: Int,
        timestamp: Long
    ) = client.get("${AppConfig.Api.CHAT_API_URL}/${userId.encodeURLPath()}/chats") {
        parameter("limit", limit)
        parameter("timestamp", timestamp)
    }.body<List<ChatDto>>()

    suspend fun getPreviousChats(
        userId: String,
        limit: Int,
        offset: Int
    ) = client.get("${AppConfig.Api.CHAT_API_URL}/${userId.encodeURLPath()}/chats") {
        parameter("limit", limit)
        parameter("offset", offset)
    }.body<List<ChatDto>>()

    suspend fun getPreviousMessages(
        userId: String,
        chatId: String,
        limit: Int,
        timestamp: Long
    ) = client.get("${AppConfig.Api.CHAT_API_URL}/${userId.encodeURLPath()}/chats/$chatId/messages") {
        parameter("limit", limit)
        parameter("timestamp", timestamp)
    }.body<List<MessageDto>>()

    suspend fun getMessages(
        userId: String,
        chatId: String,
        limit: Int,
        offset: Int
    ) = client.get("${AppConfig.Api.CHAT_API_URL}/${userId.encodeURLPath()}/chats/$chatId/messages") {
        parameter("offset", offset)
        parameter("limit", limit)
    }.body<List<MessageDto>>()

    suspend fun getMessages(
        userId: String,
        chatId: String
    ) = client.get("${AppConfig.Api.CHAT_API_URL}/${userId.encodeURLPath()}/chats/$chatId/messages").body<List<MessageDto>>()

    fun getChangedMessagesByUser(userId: String) = client.tryToExecuteWebSocket<List<MessageDto>>("${AppConfig.Api.CHAT_API_URL}/${userId.encodeURLPath()}/messages")
}
