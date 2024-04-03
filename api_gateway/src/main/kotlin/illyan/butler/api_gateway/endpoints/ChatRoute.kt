package illyan.butler.api_gateway.endpoints

import illyan.butler.api_gateway.data.model.chat.ChatDto
import illyan.butler.api_gateway.data.model.chat.MessageDto
import illyan.butler.api_gateway.data.service.ChatService
import illyan.butler.api_gateway.endpoints.utils.ChatSocketHandler
import illyan.butler.api_gateway.endpoints.utils.WebSocketServerHandler
import illyan.butler.api_gateway.utils.Claim
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
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
    val chatSocketHandler: ChatSocketHandler by inject()

    authenticate("auth-jwt") {
        route("/chats") {
            get {
                val userId = call.principal<JWTPrincipal>()?.payload?.getClaim(Claim.USER_ID).toString().trim('\"', ' ')
                val limit = call.parameters["limit"]?.toInt()
                val timestamp = call.parameters["timestamp"]?.toLong()
                call.respond(HttpStatusCode.OK, if (limit == null || timestamp == null) {
                    chatService.getChats(userId)
                } else {
                    chatService.getPreviousChats(userId, limit, timestamp)
                })
            }

            post {
                val userId = call.principal<JWTPrincipal>()?.payload?.getClaim(Claim.USER_ID).toString().trim('\"', ' ')
                val chat = call.receive<ChatDto>()
                val result = chatService.createChat(userId, chat)
                call.respond(HttpStatusCode.Created, result)
            }

            webSocket {
                val userId = call.principal<JWTPrincipal>()?.payload?.getClaim(Claim.USER_ID).toString().trim('\"', ' ')
                val chats = chatService.receiveChats(userId)
                webSocketServerHandler.sessions[userId] = this
                webSocketServerHandler.sessions[userId]?.let {
                    webSocketServerHandler.tryToCollect(chats, it)
                }
            }

            route("/{chatId}") {
                get {
                    val userId = call.principal<JWTPrincipal>()?.payload?.getClaim(Claim.USER_ID).toString().trim('\"', ' ')
                    val chatId = call.parameters["chatId"]?.trim().orEmpty()
                    val result = chatService.getChat(userId, chatId)
                    call.respond(HttpStatusCode.OK, result)
                }

                put {
                    val userId = call.principal<JWTPrincipal>()?.payload?.getClaim(Claim.USER_ID).toString().trim('\"', ' ')
                    val chatId = call.parameters["chatId"]?.trim().orEmpty()
                    val chat = call.receive<ChatDto>()
                    val result = chatService.editChat(userId, chatId, chat)
                    call.respond(HttpStatusCode.OK, result)
                }

                delete {
                    val userId = call.principal<JWTPrincipal>()?.payload?.getClaim(Claim.USER_ID).toString().trim('\"', ' ')
                    val chatId = call.parameters["chatId"]?.trim().orEmpty()
                    val result = chatService.deleteChat(userId, chatId)
                    call.respond(HttpStatusCode.OK, result)
                }

                webSocket {
                    val userId = call.principal<JWTPrincipal>()?.payload?.getClaim(Claim.USER_ID).toString().trim('\"', ' ')
                    val chatId = call.parameters["chatId"]?.trim().orEmpty()
                    val chatMessages = chatService.receiveMessages(userId, chatId)
                    webSocketServerHandler.sessions[chatId] = this
                    webSocketServerHandler.sessions[chatId]?.let {
                        webSocketServerHandler.tryToCollect(chatMessages, it)
                    }
                }

                route("/messages") {
                    get {
                        val userId = call.principal<JWTPrincipal>()?.payload?.getClaim(Claim.USER_ID).toString().trim('\"', ' ')
                        val chatId = call.parameters["chatId"]?.trim().orEmpty()
                        val limit = call.parameters["limit"]?.toInt()
                        val timestamp = call.parameters["timestamp"]?.toLong()
                        val offset = call.parameters["offset"]?.toInt()
                        call.respond(HttpStatusCode.OK, if (limit != null) {
                            if (offset != null) {
                                chatService.getMessages(userId, chatId, limit, offset)
                            } else if (timestamp != null) {
                                chatService.getPreviousMessages(userId, chatId, limit, timestamp)
                            } else {
                                chatService.getMessages(userId, chatId)
                            }
                        } else chatService.getMessages(userId, chatId))
                    }

                    post {
                        val userId = call.principal<JWTPrincipal>()?.payload?.getClaim(Claim.USER_ID).toString().trim('\"', ' ')
                        val message = call.receive<MessageDto>()
                        val result = chatService.sendMessage(userId, message)
                        call.respond(HttpStatusCode.Created, result)
                    }

                    route("/{messageId}") {
                        put {
                            val userId = call.principal<JWTPrincipal>()?.payload?.getClaim(Claim.USER_ID).toString().trim('\"', ' ')
                            val message = call.receive<MessageDto>()
                            val messageId = call.parameters["messageId"]?.trim().orEmpty()
                            val result = chatService.editMessage(userId, messageId, message)
                            call.respond(HttpStatusCode.OK, result)
                        }

                        delete {
                            val userId = call.principal<JWTPrincipal>()?.payload?.getClaim(Claim.USER_ID).toString().trim('\"', ' ')
                            val chatId = call.parameters["chatId"]?.trim().orEmpty()
                            val messageId = call.parameters["messageId"]?.trim().orEmpty()
                            val result = chatService.deleteMessage(userId, chatId, messageId)
                            call.respond(HttpStatusCode.OK, result)
                        }
                    }
                }
            }
        }
    }
}