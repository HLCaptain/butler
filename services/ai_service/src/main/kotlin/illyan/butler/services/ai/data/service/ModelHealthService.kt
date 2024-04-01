package illyan.butler.services.ai.data.service

import illyan.butler.services.ai.AppConfig
import illyan.butler.services.ai.data.model.openai.Model
import illyan.butler.services.ai.data.model.openai.ModelsResponse
import io.github.aakira.napier.Napier
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.annotation.Single

@Single
class ModelHealthService(
    private val client: HttpClient,
    coroutineScope: CoroutineScope
) {
    private val _models = MutableStateFlow<List<Model>?>(null)
    val healthyModels = _models.asStateFlow()

    init {
        coroutineScope.launch {
            combine(
                AppConfig.Api.OPEN_AI_API_URLS.map { url ->
                    // TODO: open websocket to each URL and get model ids and health status
                    flow<List<Model>> {
                        while (true) {
                            emit(client.get("$url/models").body<ModelsResponse>().data)
                            delay(1000L)
                        }
                    }
                }
            ) {
                it.reduce { acc, list -> acc + list }
            }.collectLatest { models ->
                _models.update {
                    models.distinct().also { Napier.d { "Available models: $it" } }
                }
            }
        }
    }
}