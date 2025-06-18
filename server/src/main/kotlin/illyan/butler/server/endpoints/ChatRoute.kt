package illyan.butler.server.endpoints

import illyan.butler.server.AppConfig
import illyan.butler.server.data.service.ChatService
import illyan.butler.server.utils.Claim
import illyan.butler.shared.llm.LlmService
import illyan.butler.shared.model.chat.ChatDto
import illyan.butler.shared.model.chat.MessageDto
import illyan.butler.shared.model.chat.ResourceDto
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
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
import io.ktor.server.websocket.DefaultWebSocketServerSession
import io.ktor.server.websocket.webSocket
import io.ktor.websocket.Frame
import io.ktor.websocket.close
import io.ktor.websocket.readText
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromHexString
import kotlinx.serialization.encodeToHexString
import kotlinx.serialization.json.Json
import kotlinx.serialization.protobuf.ProtoBuf
import org.koin.ktor.ext.inject

@OptIn(ExperimentalSerializationApi::class)
private suspend inline fun <reified T : Any> DefaultWebSocketServerSession.sendSerialized(data: T) {
    val serializedText = when (AppConfig.Ktor.DEFAULT_CONTENT_TYPE) {
        ContentType.Application.Json -> Json.encodeToString(data)
        ContentType.Application.ProtoBuf -> ProtoBuf.encodeToHexString(data)
        else -> Json.encodeToString(data)
    }
    send(Frame.Text(serializedText))
}

@OptIn(ExperimentalSerializationApi::class)
private inline fun <reified T : Any> DefaultWebSocketServerSession.tryReceiveDeserialized(frameText: String): T? {
    return try {
        when (AppConfig.Ktor.DEFAULT_CONTENT_TYPE) {
            ContentType.Application.Json -> Json.decodeFromString<T>(frameText)
            ContentType.Application.ProtoBuf -> ProtoBuf.decodeFromHexString<T>(frameText)
            else -> Json.decodeFromString<T>(frameText)
        }
    } catch (e: Exception) {
        // Log error or handle deserialization failure
        println("Failed to deserialize: ${e.message}")
        null
    }
}

@OptIn(ExperimentalSerializationApi::class)
fun Route.chatRoute() {
    val chatService: ChatService by inject()
    val llmService: LlmService by inject()

    authenticate("auth-jwt") {
        route("/messages") {
            get {
                val userId = call.principal<JWTPrincipal>()?.payload?.getClaim(Claim.USER_ID).toString().trim('\"', ' ')
                call.respond(chatService.getMessages(userId))
            }
        }

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

            route("/{chatId}") {
                get {
                    val userId = call.principal<JWTPrincipal>()?.payload?.getClaim(Claim.USER_ID).toString().trim('\"', ' ')
                    val chatId = call.parameters["chatId"]?.trim().orEmpty()
                    val result = chatService.getChat(userId, chatId)
                    call.respond(HttpStatusCode.OK, result)
                }

                webSocket {
                    val userId = call.principal<JWTPrincipal>()?.payload?.getClaim(Claim.USER_ID).toString().trim('\"', ' ')
                    val chatId = call.parameters["chatId"]?.trim().orEmpty()

                    if (userId.isEmpty() || chatId.isEmpty()) {
                        close(io.ktor.websocket.CloseReason(io.ktor.websocket.CloseReason.Codes.VIOLATED_POLICY, "User ID and Chat ID must be provided"))
                        return@webSocket
                    }

                    launch {
                        chatService.getChangesFromChat(userId, chatId).collectLatest { chatDto ->
                            sendSerialized(chatDto)
                        }
                    }

                    for (frame in incoming) {
                        // Handle incoming frames if necessary, e.g., client acknowledging receipt or sending commands
                    }
                }

                put {
                    val userId = call.principal<JWTPrincipal>()?.payload?.getClaim(Claim.USER_ID).toString().trim('\"', ' ')
                    val chatId = call.parameters["chatId"]?.trim().orEmpty()
                    val chat = call.receive<ChatDto>()
                    require(chat.id == chatId) { "Chat id must match path parameter" }
                    val result = chatService.editChat(userId, chat)
                    call.respond(HttpStatusCode.OK, result)
                }

                delete {
                    val userId = call.principal<JWTPrincipal>()?.payload?.getClaim(Claim.USER_ID).toString().trim('\"', ' ')
                    val chatId = call.parameters["chatId"]?.trim().orEmpty()
                    val result = chatService.deleteChat(userId, chatId)
                    call.respond(HttpStatusCode.OK, result)
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

                    webSocket {
                        val userId = call.principal<JWTPrincipal>()?.payload?.getClaim(Claim.USER_ID).toString().trim('\"', ' ')
                        val chatId = call.parameters["chatId"]?.trim().orEmpty()

                        if (userId.isEmpty() || chatId.isEmpty()) {
                            close(io.ktor.websocket.CloseReason(io.ktor.websocket.CloseReason.Codes.VIOLATED_POLICY, "User ID and Chat ID must be provided"))
                            return@webSocket
                        }

                        // Launch a coroutine to send all messages history once and then listen for new messages
                        launch {
                            // Send all current messages once upon connection
                            val initialMessages = chatService.getMessages(userId, chatId)
                            sendSerialized(initialMessages)

                            // Then, collect and send new/changed messages
                            chatService.getChangedMessagesByChat(userId, chatId).collectLatest { messages ->
                                sendSerialized(messages)
                            }
                        }

                        // Handle incoming messages from the client
                        for (frame in incoming) {
                            if (frame is Frame.Text) {
                                val text = frame.readText()
                                val messageDto = tryReceiveDeserialized<MessageDto>(text)
                                if (messageDto != null) {
                                    // Ensure the received message belongs to the correct chat and user before processing
                                    if (messageDto.chatId == chatId) {
                                        chatService.sendMessage(userId, messageDto.copy(senderId = userId))
                                        // The flow above will pick up the new message and broadcast it
                                    } else {
                                        // Log or handle messages for wrong chat
                                    }
                                }
                            }
                        }
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
                            require(message.id == messageId) { "Message id must match path parameter" }
                            val result = chatService.editMessage(userId, message)
                            call.respond(HttpStatusCode.OK, result)
                        }

                        // Connecting to Chat Service with "AI" service account
                        // Listen to chats with custom Chat Service models with WebSocket
                        // Send message to Chat Service
                        // Cache chat history in memory
                        // Regenerate message or generate new one when prompted
                        // TODO: Implement robust message regeneration logic

                        // Average AI message size: 2.78 KB
                        // Average user message size: 0.2 KB
                        // Average interaction back and forth: 3 KB
                        // Long chat history: 10 * 3 KB = 30 KB
                        // DAU: 10000
                        // Daily chat history: 30 KB * 10000 = 300 MB

                        // https://www.pugetsystems.com/labs/hpc/benchmarking-with-tensorrt-llm/
                        // 4090 LLaMA 2 7B_q4_0 tokens/s: 150 ~ 150 bytes (LOW model)
                        // 540 KB/hour data retrieval ~ 13 MB/day
                        // DAU average 10 messages/day
                        // 30 KB/DAU/day -> ~433 DAU can be supported by a single 4090, ~2780 tokens/user/day
                        // Electric cost: 0.1 USD/hour, 4090 draws 450W ~ 500W with PC on -> 0.5 * 24 * 0.1 = 1.2 USD/day, ~36 USD/month (0.08314/user/month)
                        // 2780 * 30 = 83,400 tokens/month
                        // https://app.endpoints.anyscale.com/
                        // https://openrouter.ai/
                        // Average price /M tokens: 0.50 USD
                        // 83,400 * 0.50 / 1M = 0.0417 USD / USER / MONTH (MID models)

                        // Best case scenario: 10000 DAU * 0.0417 USD / MONTH = 417 USD / MONTH

                        // TODO: calculate accumulation of tokens in a single chat (more input tokens per message)

                        get("/regenerate") {
                            val chatId = call.parameters["chatId"] ?: return@get call.respond(HttpStatusCode.BadRequest)
                            val messageId = call.parameters["messageId"]
                            val userId = call.principal<JWTPrincipal>()?.payload?.getClaim(Claim.USER_ID).toString().trim('\"', ' ')
                            val chats = chatService.getChats(userId)
//                            call.respond(
//                                llmService.answerChat(
//                                    chats.first { it.id == chatId },
//                                    chats.filter { it.id != chatId },
//                                    chatService.getMessages(userId, chatId).first { it.id == messageId }
//                                )
//                            )
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

        route("/resources") {
            get {
                val userId = call.principal<JWTPrincipal>()?.payload?.getClaim(Claim.USER_ID).toString().trim('\"', ' ')
                call.respond(HttpStatusCode.OK, chatService.getResources(userId))
            }
            route("/{resourceId}") {
                get {
                    val userId = call.principal<JWTPrincipal>()?.payload?.getClaim(Claim.USER_ID).toString().trim('\"', ' ')
                    val resourceId = call.parameters["resourceId"]?.trim().orEmpty()
                    call.respond(HttpStatusCode.OK, chatService.getResource(userId, resourceId))
                }
            }

            post {
                val userId = call.principal<JWTPrincipal>()?.payload?.getClaim(Claim.USER_ID).toString().trim('\"', ' ')
                val resource = call.receive<ResourceDto>()
                call.respond(HttpStatusCode.Created, chatService.createResource(userId, resource))
            }
        }
    }
}
