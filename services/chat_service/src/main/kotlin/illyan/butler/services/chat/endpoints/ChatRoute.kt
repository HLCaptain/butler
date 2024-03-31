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
import kotlinx.datetime.Clock
import org.koin.ktor.ext.inject
import kotlin.time.Duration.Companion.days

fun Route.chatRoute() {
    val chatService: ChatService by inject()
    val webSocketServerHandler: WebSocketServerHandler by inject()

    route("/{userId}") {
        webSocket("/messages") {
            val userId = call.parameters["userId"] ?: return@webSocket call.respond(HttpStatusCode.BadRequest)
            val chats = chatService.getChangedMessagesByUser(userId)
            webSocketServerHandler.sessions.getOrPut("messages:$userId") { this }?.let {
                webSocketServerHandler.tryToCollect(chats, it)
            }
        }

        route("/chats") {
            get {
                val userId = call.parameters["userId"] ?: return@get call.respond(HttpStatusCode.BadRequest)
                val limit = call.parameters["limit"]?.toInt()
                val offset = call.parameters["offset"]?.toInt()
                val timestamp = call.parameters["timestamp"]?.toLong()
                if (limit != null) {
                    if (offset != null)
                        call.respond(chatService.getPreviousChats(userId, limit, offset))
                    else if (timestamp != null)
                        call.respond(chatService.getPreviousChats(userId, limit, timestamp))
                } else {
                    call.respond(chatService.getChats(userId))
                }
            }

            post {
                val userId = call.parameters["userId"] ?: return@post call.respond(HttpStatusCode.BadRequest)
                val chat = call.receive<ChatDto>()
                call.respond(chatService.createChat(userId, chat))
            }

            webSocket {
                val userId = call.parameters["userId"] ?: return@webSocket call.respond(HttpStatusCode.BadRequest)
                val chats = chatService.getChangedChatsAffectingUser(userId)
                webSocketServerHandler.sessions.getOrPut("chats:$userId") { this }?.let {
                    webSocketServerHandler.tryToCollect(chats, it)
                }
            }

            route("/{chatId}") {
                get {
                    val userId = call.parameters["userId"] ?: return@get call.respond(HttpStatusCode.BadRequest)
                    val chatId = call.parameters["chatId"] ?: return@get call.respond(HttpStatusCode.BadRequest)
                    call.respond(chatService.getChat(userId, chatId))
                }

                webSocket {
                    val chatId = call.parameters["chatId"] ?: return@webSocket call.respond(HttpStatusCode.BadRequest)
                    val messages = chatService.getChangedMessagesByChat(chatId)
                    webSocketServerHandler.sessions.getOrPut(chatId) { this }?.let {
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
}