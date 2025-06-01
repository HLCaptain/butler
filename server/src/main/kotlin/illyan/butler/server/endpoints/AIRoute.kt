package illyan.butler.server.endpoints

import illyan.butler.server.AppConfig
import illyan.butler.shared.llm.mapToModels
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
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.koin.ktor.ext.inject
import kotlin.time.Duration.Companion.minutes

fun Route.aiRoute() {
    authenticate("auth-jwt") {
        val coroutineScopeIO: CoroutineScope by inject()

        val availableModels = AppConfig.Api.OPEN_AI_API_URLS_AND_KEYS.mapToModels(pingDuration = 1.minutes)
            .stateIn(coroutineScopeIO, SharingStarted.Eagerly, null)

        route("/models") {
            get {
                call.respond(HttpStatusCode.OK, availableModels.firstOrNull().orEmpty())
            }

            webSocket {
                coroutineScopeIO.launch {
                    availableModels.collect { models ->
                        sendSerialized(models)
                    }
                }
            }

            get("/{modelId}") {
                val modelId = call.parameters["modelId"]?.trim().orEmpty()
                availableModels.value.let { providerModels ->
                    val modelsWithId = providerModels
                        ?.filter { it.id == modelId }
                        ?.map { it.endpoint }
                    modelsWithId?.let {
                        call.respond(HttpStatusCode.OK, modelsWithId)
                    } ?: call.respond(HttpStatusCode.NotFound)
                }
            }
        }

        get("/providers") {
            call.respond(
                HttpStatusCode.OK,
                availableModels.value?.map { it.endpoint }?.distinct().orEmpty()
            )
        }
    }
}
