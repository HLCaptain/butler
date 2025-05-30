package illyan.butler.server.endpoints

import illyan.butler.server.AppConfig
import illyan.butler.shared.llm.mapToModelsAndProviders
import illyan.butler.shared.llm.mapToProvidedModels
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import io.ktor.server.websocket.sendSerialized
import io.ktor.server.websocket.webSocket
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.koin.ktor.ext.inject
import kotlin.time.Duration.Companion.minutes

fun Route.aiRoute() {
    authenticate("auth-jwt") {
        val coroutineScopeIO: CoroutineScope by inject()

        val availableModelsFromProviders = AppConfig.Api.OPEN_AI_API_URLS_AND_KEYS.mapToProvidedModels(pingDuration = 1.minutes)
            .stateIn(coroutineScopeIO, SharingStarted.Eagerly, emptyMap())
        val modelsAndProviders = availableModelsFromProviders.mapToModelsAndProviders()
            .stateIn(coroutineScopeIO, SharingStarted.Eagerly, emptyMap())

        route("/models") {
            get {
                call.respond(HttpStatusCode.OK, modelsAndProviders.first())
            }

            webSocket {
                coroutineScopeIO.launch {
                    modelsAndProviders.collect { models ->
                        sendSerialized(models)
                    }
                }
            }

            get("/{modelId}") {
                val modelId = call.parameters["modelId"]?.trim().orEmpty()
                availableModelsFromProviders.value.let { providerModels ->
                    val model = providerModels.values.firstNotNullOfOrNull { models -> models.orEmpty().firstOrNull { it.id == modelId } }
                    val providersOfModel = providerModels.flatMap { (provider, models) ->
                        if (models.orEmpty().any { it.id == modelId }) listOf(provider) else emptyList()
                    }
                    model?.let {
                        call.respond(HttpStatusCode.OK, it to providersOfModel)
                    } ?: call.respond(HttpStatusCode.NotFound)
                }
            }
        }

        get("/providers") {
            call.respond(
                HttpStatusCode.OK,
                availableModelsFromProviders.value
            )
        }
    }
}
