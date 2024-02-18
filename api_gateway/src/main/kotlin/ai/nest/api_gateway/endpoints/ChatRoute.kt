package ai.nest.api_gateway.endpoints

import ai.nest.api_gateway.data.model.chat.MessageDto
import ai.nest.api_gateway.data.service.ChatService
import ai.nest.api_gateway.endpoints.utils.ChatSocketHandler
import ai.nest.api_gateway.endpoints.utils.WebSocketServerHandler
import ai.nest.api_gateway.endpoints.utils.respondWithResult
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import io.ktor.server.websocket.webSocket
import org.koin.ktor.ext.inject

fun Route.chatRoute() {

    val chatService: ChatService by inject()
    val webSocketServerHandler: WebSocketServerHandler by inject()
    val chatSocketHandler: ChatSocketHandler by inject()

    route("/chats") {
        post("/{chatId}") {
            val chatId = call.parameters["chatId"]?.trim().orEmpty()
            val message = call.receive<MessageDto>()
            val result = chatService.sendChatMessage(message, chatId)
            respondWithResult(HttpStatusCode.OK, result)
        }
        webSocket("/{chatId}") {
            val chatId = call.parameters["chatId"]?.trim().orEmpty()
            val chatMessages = chatService.receiveChatMessages(chatId)
            webSocketServerHandler.sessions[chatId] = this
            webSocketServerHandler.sessions[chatId]?.let {
                webSocketServerHandler.tryToCollect(chatMessages, it)
            }
        }
    }
}