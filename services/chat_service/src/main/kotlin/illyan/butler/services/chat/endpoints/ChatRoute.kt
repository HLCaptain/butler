package illyan.butler.services.chat.endpoints

import illyan.butler.services.chat.data.model.chat.ChatDto
import illyan.butler.services.chat.data.model.chat.MessageDto
import illyan.butler.services.chat.data.model.chat.ResourceDto
import illyan.butler.services.chat.data.service.ChatService
import illyan.butler.services.chat.endpoints.utils.WebSocketServerHandler
import io.github.aakira.napier.Napier
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

    route("/{userId}") {
        route("/messages") {
            get {
                val userId = call.parameters["userId"] ?: return@get call.respond(HttpStatusCode.BadRequest)
                call.respond(chatService.getMessages(userId))
            }

            webSocket {
                val userId = call.parameters["userId"] ?: return@webSocket call.respond(HttpStatusCode.BadRequest)
                webSocketServerHandler.addFlowSessionListener("messages:$userId", this) {
                    chatService.getChangedMessagesByUser(userId)
                }
                Napier.d("Added new message listener for user $userId")
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
                    call.respond(HttpStatusCode.OK, chatService.getChats(userId))
                }
            }

            post {
                val userId = call.parameters["userId"] ?: return@post call.respond(HttpStatusCode.BadRequest)
                val chat = call.receive<ChatDto>()
                Napier.v { "Received by user $userId a new chat instance $chat" }
                call.respond(HttpStatusCode.Created, chatService.createChat(userId, chat))
            }

            webSocket {
                val userId = call.parameters["userId"] ?: return@webSocket call.respond(HttpStatusCode.BadRequest)
                webSocketServerHandler.addFlowSessionListener("chats:$userId", this) {
                    chatService.getChangedChatsAffectingUser(userId)
                }
                Napier.v { "Added new chat listener for user $userId" }
            }

            route("/{chatId}") {
                get {
                    val userId = call.parameters["userId"] ?: return@get call.respond(HttpStatusCode.BadRequest)
                    val chatId = call.parameters["chatId"] ?: return@get call.respond(HttpStatusCode.BadRequest)
                    call.respond(chatService.getChat(userId, chatId))
                }

                put {
                    val userId = call.parameters["userId"] ?: return@put call.respond(HttpStatusCode.BadRequest)
                    val chat = call.receive<ChatDto>()
                    call.respond(chatService.editChat(userId, chat))
                }

                webSocket {
                    val userId = call.parameters["userId"] ?: return@webSocket call.respond(HttpStatusCode.BadRequest)
                    val chatId = call.parameters["chatId"] ?: return@webSocket call.respond(HttpStatusCode.BadRequest)
                    webSocketServerHandler.addFlowSessionListener(chatId, this) {
                        chatService.getChangedMessagesByChat(userId, chatId)
                    }
                    Napier.v { "Added new chat message listener for chat $chatId" }
                }

                delete {
                    val userId = call.parameters["userId"] ?: return@delete call.respond(HttpStatusCode.BadRequest)
                    val chatId = call.parameters["chatId"] ?: return@delete call.respond(HttpStatusCode.BadRequest)
                    call.respond(chatService.deleteChat(userId, chatId))
                }

                route("/messages") {
                    get {
                        val userId = call.parameters["userId"] ?: return@get call.respond(HttpStatusCode.BadRequest)
                        val chatId = call.parameters["chatId"] ?: return@get call.respond(HttpStatusCode.BadRequest)
                        val limit = call.parameters["limit"]?.toInt()
                        val timestamp = call.parameters["timestamp"]?.toLong()
                        call.respond(HttpStatusCode.OK, if (timestamp == null || limit == null) {
                            call.respond(chatService.getMessages(userId, chatId))
                        } else {
                            call.respond(chatService.getPreviousMessages(userId, chatId, limit, timestamp))
                        })
                    }

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
        route("/resources") {
            get {
                val userId = call.parameters["userId"] ?: return@get call.respond(HttpStatusCode.BadRequest)
                call.respond(chatService.getResources(userId))
            }
            route("/{resourceId}") {
                get {
                    val userId = call.parameters["userId"] ?: return@get call.respond(HttpStatusCode.BadRequest)
                    val resourceId = call.parameters["resourceId"] ?: return@get call.respond(HttpStatusCode.BadRequest)
                    call.respond(HttpStatusCode.OK, chatService.getResource(userId, resourceId))
                }

                delete {
                    val userId = call.parameters["userId"] ?: return@delete call.respond(HttpStatusCode.BadRequest)
                    val resourceId = call.parameters["resourceId"] ?: return@delete call.respond(HttpStatusCode.BadRequest)
                    call.respond(chatService.deleteResource(userId, resourceId))
                }
            }
            post {
                val userId = call.parameters["userId"] ?: return@post call.respond(HttpStatusCode.BadRequest)
                val resource = call.receive<ResourceDto>()
                call.respond(HttpStatusCode.Created, chatService.createResource(userId, resource))
            }
        }
    }
}