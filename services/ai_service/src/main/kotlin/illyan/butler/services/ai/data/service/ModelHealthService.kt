package illyan.butler.services.ai.data.service

import illyan.butler.services.ai.data.mapping.toModelDto
import illyan.butler.services.ai.data.model.openai.Model
import illyan.butler.services.ai.data.model.openai.ModelsResponse
import io.github.aakira.napier.Napier
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single

@Single
class ModelHealthService(
    @Named("OpenAIHttpClients") private val openAiClients: Map<String, HttpClient>,
    coroutineScope: CoroutineScope
) {
    private val _models = MutableStateFlow<Map<String, List<Model>>>(emptyMap())
    val providerModels = _models.asStateFlow()
    val modelsAndProviders = _models.map { models ->
        models.flatMap { (provider, models) ->
            models.map { it.toModelDto() to provider }
        }.groupBy { it.first.id }.map { (_, modelWithProvider) ->
            modelWithProvider.first().first to modelWithProvider.map { it.second }
        }.toMap() // Map<ModelDto, List<String>> where key is model and value is list of providers
    }.stateIn(
        coroutineScope,
        started = SharingStarted.Eagerly,
        initialValue = emptyMap()
    )

    init {
        coroutineScope.launch {
            openAiClients.map { (url, client) ->
                // TODO: open websocket to each URL and get model ids and health status
                flow {
                    while (true) {
                        try {
                            // fetching this way makes it more lenient to errors due to missing properties in JSON
                            emit(url to client.get("$url/models").body<ModelsResponse>().data)
                        } catch (e: Exception) {
                            Napier.e(e) { "Error fetching models from $url" }
                        }
                        delay(60000L)
                    }
                }
            }.merge().collectLatest { models -> _models.update { it + models } }
        }
    }
}