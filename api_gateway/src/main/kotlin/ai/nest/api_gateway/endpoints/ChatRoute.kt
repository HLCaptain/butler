package ai.nest.api_gateway.endpoints

import ai.nest.api_gateway.data.model.chat.MessageDto
import ai.nest.api_gateway.data.model.chat.TicketDto
import ai.nest.api_gateway.data.model.response.ServerResponse
import ai.nest.api_gateway.data.service.ChatService
import ai.nest.api_gateway.endpoints.utils.ChatSocketHandler
import ai.nest.api_gateway.endpoints.utils.Connection
import ai.nest.api_gateway.endpoints.utils.WebSocketServerHandler
import ai.nest.api_gateway.endpoints.utils.respondWithResult
import ai.nest.api_gateway.endpoints.utils.withPermissions
import ai.nest.api_gateway.utils.Permission
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.route
import io.ktor.server.websocket.receiveDeserialized
import io.ktor.server.websocket.sendSerialized
import io.ktor.server.websocket.webSocket
import kotlinx.coroutines.flow.collectLatest
import org.koin.ktor.ext.inject

fun Route.chatRoute() {

    val chatService: ChatService by inject()
    val webSocketServerHandler: WebSocketServerHandler by inject()
    val chatSocketHandler: ChatSocketHandler by inject()

    route("/chat") {
        withPermissions(Permission.END_USER) {
            post("/ticket") {
                val ticket = call.receive<TicketDto>()
                val result = chatService.createTicket(ticket)
                respondWithResult(HttpStatusCode.Created, result)
            }
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

        withPermissions(Permission.SUPPORT) {

            put("/{ticketId}") {
                val ticketId = call.parameters["ticketId"]?.trim().orEmpty()
                val state = call.parameters["state"].toBoolean()
                val result = chatService.updateTicketState(ticketId, state)
                respondWithResult(HttpStatusCode.OK, result)
            }

            webSocket("/tickets/{supportId}") {
                val supportId = call.parameters["supportId"]?.trim().orEmpty()
                val tickets = chatService.receiveTicket(supportId)
                webSocketServerHandler.sessions[supportId] = this
                webSocketServerHandler.sessions[supportId]?.let {
                    webSocketServerHandler.tryToCollect(tickets, it)
                }
            }
        }

        webSocket("/{ticketId}") {
            val ticketId = call.parameters["ticketId"]?.trim().orEmpty()

            if ((chatSocketHandler.connections[ticketId]?.size ?: 0) > 1) return@webSocket

            val chatConnections = chatSocketHandler.connections.computeIfAbsent(ticketId) { LinkedHashSet() }
            chatConnections.add(Connection(this))

            try {
                while (true) {
                    val receiveMessage = receiveDeserialized<MessageDto>()
                    chatService.sendAndReceiveMessage(receiveMessage, ticketId).collectLatest { message ->
                        chatSocketHandler.connections[ticketId]?.forEach {
                            it.session.sendSerialized(ServerResponse.success(message))
                        }
                    }
                }
            } catch (e: Throwable) {
                println(e.message)
            } finally {
                chatSocketHandler.connections.remove(ticketId)
            }
        }
    }
}