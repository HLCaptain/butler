package illyan.butler.api_gateway.endpoints

import illyan.butler.api_gateway.data.model.chat.ChatDto
import illyan.butler.api_gateway.data.model.chat.MessageDto
import illyan.butler.api_gateway.data.service.ChatService
import illyan.butler.api_gateway.endpoints.utils.ChatSocketHandler
import illyan.butler.api_gateway.endpoints.utils.WebSocketServerHandler
import illyan.butler.api_gateway.endpoints.utils.respondWithResult
import illyan.butler.api_gateway.utils.Claim
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
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

    route("/chats") {
        get {
            val userId = call.principal<JWTPrincipal>()?.payload?.getClaim(Claim.USER_ID).toString()
            val limit = call.parameters["limit"]?.toInt() ?: 10
            val timestamp = call.parameters["timestamp"]?.toLong() ?: System.currentTimeMillis()
            val result = chatService.getPreviousChats(userId, limit, timestamp)
            respondWithResult(HttpStatusCode.OK, result)
        }

        post {
            val userId = call.principal<JWTPrincipal>()?.payload?.getClaim(Claim.USER_ID).toString()
            val chat = call.receive<ChatDto>()
            val result = chatService.createChat(userId, chat)
            respondWithResult(HttpStatusCode.Created, result)
        }

        webSocket {
            val userId = call.principal<JWTPrincipal>()?.payload?.getClaim(Claim.USER_ID).toString()
            val chats = chatService.receiveChats(userId)
            webSocketServerHandler.sessions[userId] = this
            webSocketServerHandler.sessions[userId]?.let {
                webSocketServerHandler.tryToCollect(chats, it)
            }
        }

        route("/{chatId}") {
            get {
                val userId = call.principal<JWTPrincipal>()?.payload?.getClaim(Claim.USER_ID).toString()
                val chatId = call.parameters["chatId"]?.trim().orEmpty()
                val result = chatService.getChat(userId, chatId)
                respondWithResult(HttpStatusCode.OK, result)
            }

            put {
                val userId = call.principal<JWTPrincipal>()?.payload?.getClaim(Claim.USER_ID).toString()
                val chatId = call.parameters["chatId"]?.trim().orEmpty()
                val chat = call.receive<ChatDto>()
                val result = chatService.editChat(userId, chatId, chat)
                respondWithResult(HttpStatusCode.OK, result)
            }

            delete {
                val userId = call.principal<JWTPrincipal>()?.payload?.getClaim(Claim.USER_ID).toString()
                val chatId = call.parameters["chatId"]?.trim().orEmpty()
                val result = chatService.deleteChat(userId, chatId)
                respondWithResult(HttpStatusCode.OK, result)
            }

            webSocket {
                val userId = call.principal<JWTPrincipal>()?.payload?.getClaim(Claim.USER_ID).toString()
                val chatId = call.parameters["chatId"]?.trim().orEmpty()
                val chatMessages = chatService.receiveMessages(userId, chatId)
                webSocketServerHandler.sessions[chatId] = this
                webSocketServerHandler.sessions[chatId]?.let {
                    webSocketServerHandler.tryToCollect(chatMessages, it)
                }
            }

            route("/messages") {
                get {
                    val userId = call.principal<JWTPrincipal>()?.payload?.getClaim(Claim.USER_ID).toString()
                    val chatId = call.parameters["chatId"]?.trim().orEmpty()
                    val limit = call.parameters["limit"]?.toInt() ?: 10
                    val timestamp = call.parameters["timestamp"]?.toLong() ?: System.currentTimeMillis()
                    val result = chatService.getPreviousMessages(userId, chatId, limit, timestamp)
                    respondWithResult(HttpStatusCode.OK, result)
                }

                post {
                    val userId = call.principal<JWTPrincipal>()?.payload?.getClaim(Claim.USER_ID).toString()
                    val message = call.receive<MessageDto>()
                    val result = chatService.sendMessage(userId, message)
                    respondWithResult(HttpStatusCode.Created, result)
                }

                route("/{messageId}") {
                    put {
                        val userId = call.principal<JWTPrincipal>()?.payload?.getClaim(Claim.USER_ID).toString()
                        val message = call.receive<MessageDto>()
                        val messageId = call.parameters["messageId"]?.trim().orEmpty()
                        val result = chatService.editMessage(userId, messageId, message)
                        respondWithResult(HttpStatusCode.OK, result)
                    }

                    delete {
                        val userId = call.principal<JWTPrincipal>()?.payload?.getClaim(Claim.USER_ID).toString()
                        val chatId = call.parameters["chatId"]?.trim().orEmpty()
                        val messageId = call.parameters["messageId"]?.trim().orEmpty()
                        val result = chatService.deleteMessage(userId, chatId, messageId)
                        respondWithResult(HttpStatusCode.OK, result)
                    }
                }
            }
        }
    }
}