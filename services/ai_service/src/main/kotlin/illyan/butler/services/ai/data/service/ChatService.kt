package illyan.butler.services.ai.data.service

import illyan.butler.services.ai.AppConfig
import illyan.butler.services.ai.data.model.chat.ChatDto
import illyan.butler.services.ai.data.model.chat.MessageDto
import illyan.butler.services.ai.data.model.response.PaginationResponse
import illyan.butler.services.ai.data.utils.getLastMonthDate
import illyan.butler.services.ai.data.utils.getLastWeekDate
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.websocket.receiveDeserialized
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.datetime.Clock
import org.koin.core.annotation.Single

@Single
class ChatService(private val client: HttpClient) {
    fun receiveMessages(userId: String, chatId: String) = flow {
        client.webSocket("${AppConfig.Api.CHAT_API_URL}/$userId/chats/$chatId") {
            incoming.receiveAsFlow().collectLatest { emit(receiveDeserialized<List<MessageDto>>()) }
        }
    }
    fun receiveMessages(userId: String) = flow {
        client.webSocket("${AppConfig.Api.CHAT_API_URL}/$userId/messages") {
            incoming.receiveAsFlow().collectLatest { emit(receiveDeserialized<List<MessageDto>>()) }
        }
    }
    suspend fun sendMessage(userId: String, message: MessageDto) = client.post("${AppConfig.Api.CHAT_API_URL}/$userId/chats/${message.chatId}/messages") { setBody(message) }.body<MessageDto>()
    suspend fun editMessage(userId: String, messageId: String, message: MessageDto) = client.put("${AppConfig.Api.CHAT_API_URL}/$userId/chats/${message.chatId}/messages/$messageId") { setBody(message) }.body<MessageDto>()
    suspend fun deleteMessage(userId: String, chatId: String, messageId: String) = client.delete("${AppConfig.Api.CHAT_API_URL}/$userId/chats/$chatId/messages/$messageId").body<Boolean>()

    suspend fun getChatMessages(userId: String) = flow {
        client.webSocket("${AppConfig.Api.CHAT_API_URL}/$userId/chats") {
            incoming.receiveAsFlow().collectLatest { emit(receiveDeserialized<List<MessageDto>>()) }
        }
    }
    suspend fun getChat(userId: String, chatId: String) = client.get("${AppConfig.Api.CHAT_API_URL}/$userId/chats/$chatId").body<ChatDto>()
    suspend fun createChat(userId: String, chat: ChatDto) = client.post("${AppConfig.Api.CHAT_API_URL}/$userId/chats") { setBody(chat) }.body<ChatDto>()
    suspend fun editChat(userId: String, chatId: String, chat: ChatDto) = client.put("${AppConfig.Api.CHAT_API_URL}/$userId/chats/$chatId") { setBody(chat) }.body<ChatDto>()
    suspend fun deleteChat(userId: String, chatId: String) = client.delete("${AppConfig.Api.CHAT_API_URL}/$userId/chats/$chatId").body<Boolean>()

    fun getChangedChatsAffectingUser(userId: String) = flow {
        client.webSocket("${AppConfig.Api.CHAT_API_URL}/$userId/chats") {
            incoming.receiveAsFlow().collectLatest { emit(receiveDeserialized<List<ChatDto>>()) }
        }
    }

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
    }.body<PaginationResponse<ChatDto>>()

    suspend fun getChats(userId: String) = client.get("${AppConfig.Api.CHAT_API_URL}/$userId/chats").body<List<ChatDto>>()

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
    }.body<List<ChatDto>>()

    suspend fun getPreviousChats(
        userId: String,
        limit: Int,
        timestamp: Long
    ) = client.get("${AppConfig.Api.CHAT_API_URL}/$userId/chats") {
        parameter("limit", limit)
        parameter("timestamp", timestamp)
    }.body<List<ChatDto>>()

    suspend fun getPreviousChats(
        userId: String,
        limit: Int,
        offset: Int
    ) = client.get("${AppConfig.Api.CHAT_API_URL}/$userId/chats") {
        parameter("limit", limit)
        parameter("offset", offset)
    }.body<List<ChatDto>>()

    suspend fun getPreviousMessages(
        userId: String,
        chatId: String,
        limit: Int,
        timestamp: Long
    ) = client.get("${AppConfig.Api.CHAT_API_URL}/$userId/chats/$chatId/messages") {
        parameter("limit", limit)
        parameter("timestamp", timestamp)
    }.body<List<MessageDto>>()

    suspend fun getMessages(
        userId: String,
        chatId: String,
        limit: Int,
        offset: Int
    ) = client.get("${AppConfig.Api.CHAT_API_URL}/$userId/chats/$chatId/messages") {
        parameter("offset", offset)
        parameter("limit", limit)
    }.body<List<MessageDto>>()
}
