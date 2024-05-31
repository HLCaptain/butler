package illyan.butler.services.ai.endpoints

import illyan.butler.services.ai.data.mapping.toModelDto
import illyan.butler.services.ai.data.service.ModelHealthService
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import kotlinx.coroutines.flow.first
import org.koin.ktor.ext.inject

fun Route.modelRoute() {
    val modelHealthService: ModelHealthService by inject()

    route("/models") {
        get {
            call.respond(HttpStatusCode.OK, modelHealthService.modelsAndProviders.first())
        }

        get("/{modelId}") {
            val modelId = call.parameters["modelId"]?.trim().orEmpty()
            modelHealthService.providerModels.value.let { providerModels ->
                val model = providerModels.values.firstNotNullOfOrNull { models -> models.firstOrNull { it.id == modelId } }
                val providersOfModel = providerModels.flatMap { (provider, models) ->
                    if (models.any { it.id == modelId }) listOf(provider) else emptyList()
                }
                model?.let {
                    call.respond(HttpStatusCode.OK, it.toModelDto() to providersOfModel)
                } ?: call.respond(HttpStatusCode.NotFound)
            }
        }
    }
    route("/providers") {
        get {
            call.respond(
                HttpStatusCode.OK,
                modelHealthService.providerModels.value.map { it.key }
            )
        }
    }
}
