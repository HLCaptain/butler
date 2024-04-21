package illyan.butler.services.ai.endpoints

import illyan.butler.services.ai.data.service.LlmService
import illyan.butler.services.ai.endpoints.utils.WebSocketServerHandler
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import org.koin.ktor.ext.inject

fun Route.chatRoute() {
    val llmService: LlmService by inject()
    val webSocketServerHandler: WebSocketServerHandler by inject()

    // Connecting to Chat Service with "AI" service account
    // Listen to chats with custom Chat Service endpoints with WebSocket
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

    route("/chats/{chatId}") {
        get("/regenerate") {
            val chatId = call.parameters["chatId"] ?: return@get call.respond(HttpStatusCode.BadRequest)
            val messageId = call.parameters["messageId"]
            call.respond(llmService.regenerateMessage(chatId, messageId))
        }
    }
}