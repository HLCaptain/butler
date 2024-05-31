package illyan.butler.api_gateway.endpoints

import illyan.butler.api_gateway.data.service.AIService
import illyan.butler.api_gateway.endpoints.utils.WebSocketServerHandler
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.auth.authenticate
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import org.koin.ktor.ext.inject

fun Route.aiRoute() {
    val aiService: AIService by inject()
    val webSocketServerHandler: WebSocketServerHandler by inject()

    authenticate("auth-jwt") {
        route("/models") {
            get {
                call.respond(HttpStatusCode.OK, aiService.getAIModels())
            }

            get("/{modelId}") {
                val modelId = call.parameters["modelId"]?.trim().orEmpty()
                call.respond(HttpStatusCode.OK, aiService.getAIModel(modelId))
            }
        }

        get("/providers") {
            call.respond(HttpStatusCode.OK, aiService.getAIModelProviders())
        }
    }
}