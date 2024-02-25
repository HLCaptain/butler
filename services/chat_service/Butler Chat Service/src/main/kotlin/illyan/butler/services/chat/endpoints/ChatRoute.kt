package illyan.butler.services.chat.endpoints

import illyan.butler.services.chat.data.model.chat.ChatDto
import illyan.butler.services.chat.data.model.chat.MessageDto
import illyan.butler.services.chat.data.service.ChatService
import illyan.butler.services.chat.endpoints.utils.WebSocketServerHandler
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.route
import io.ktor.server.websocket.webSocket
import org.koin.ktor.ext.inject

fun Route.chatRoute() {
    val chatService: ChatService by inject()
    val webSocketServerHandler: WebSocketServerHandler by inject()

    route("/{userId}/chats") {
        get {
            val userId = call.parameters["userId"] ?: return@get call.respond(HttpStatusCode.BadRequest)
            call.respond(chatService.receiveChats(userId))
        }

        post {
            val userId = call.parameters["userId"] ?: return@post call.respond(HttpStatusCode.BadRequest)
            val chat = call.receive<ChatDto>()
            call.respond(chatService.createChat(userId, chat))
        }

        route("/{chatId}") {
            get {
                val userId = call.parameters["userId"] ?: return@get call.respond(HttpStatusCode.BadRequest)
                val chatId = call.parameters["chatId"] ?: return@get call.respond(HttpStatusCode.BadRequest)
                call.respond(chatService.getChat(userId, chatId))
            }

            webSocket {
                val userId = call.parameters["userId"] ?: return@webSocket call.respond(HttpStatusCode.BadRequest)
                val chatId = call.parameters["chatId"] ?: return@webSocket call.respond(HttpStatusCode.BadRequest)
                val messages = chatService.receiveMessages(userId, chatId)
                webSocketServerHandler.sessions[userId] = this
                webSocketServerHandler.sessions[userId]?.let {
                    webSocketServerHandler.tryToCollect(messages, it)
                }
            }

            delete {
                val userId = call.parameters["userId"] ?: return@delete call.respond(HttpStatusCode.BadRequest)
                val chatId = call.parameters["chatId"] ?: return@delete call.respond(HttpStatusCode.BadRequest)
                call.respond(chatService.deleteChat(userId, chatId))
            }

            route("/messages") {
                post {
                    val userId = call.parameters["userId"] ?: return@post call.respond(HttpStatusCode.BadRequest)
                    val message = call.receive<MessageDto>()
                    call.respond(chatService.sendMessage(userId, message))
                }

                route("/{messageId}") {
                    put {
                        val userId = call.parameters["userId"] ?: return@put call.respond(HttpStatusCode.BadRequest)
                        val message = call.receive<MessageDto>()
                        call.respond(chatService.editMessage(userId, message))
                    }

                    delete {
                        val userId = call.parameters["userId"] ?: return@delete call.respond(HttpStatusCode.BadRequest)
                        val chatId = call.parameters["chatId"] ?: return@delete call.respond(HttpStatusCode.BadRequest)
                        val messageId = call.parameters["messageId"] ?: return@delete call.respond(HttpStatusCode.BadRequest)
                        call.respond(chatService.deleteMessage(userId, chatId, messageId))
                    }
                }
            }
        }
    }
}