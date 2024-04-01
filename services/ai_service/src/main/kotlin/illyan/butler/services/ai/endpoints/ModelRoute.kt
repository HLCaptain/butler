package illyan.butler.services.ai.endpoints

import illyan.butler.services.ai.data.mapping.toModelDto
import illyan.butler.services.ai.data.service.LlmService
import illyan.butler.services.ai.data.service.ModelHealthService
import illyan.butler.services.ai.endpoints.utils.WebSocketServerHandler
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.koin.ktor.ext.inject

fun Route.modelRoute() {
    val modelHealthService: ModelHealthService by inject()

    route("/models") {
        get {
            call.respond(HttpStatusCode.OK, modelHealthService.healthyModels.first()?.map { it.toModelDto() } ?: emptyList())
        }

        get("/{modelId}") {
            val modelId = call.parameters["modelId"]?.trim().orEmpty()
            call.respond(HttpStatusCode.OK, modelHealthService.healthyModels.first()?.filter { it.id.id == modelId }?.map { it.toModelDto() } ?: emptyList())
        }
    }
}
