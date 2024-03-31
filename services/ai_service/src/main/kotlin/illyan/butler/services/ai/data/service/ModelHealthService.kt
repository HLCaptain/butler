package illyan.butler.services.ai.data.service

import com.aallam.openai.api.model.Model
import com.aallam.openai.client.OpenAI
import illyan.butler.services.ai.AppConfig
import kotlinx.coroutines.CoroutineScope
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
    private val openAI: OpenAI,
    coroutineScope: CoroutineScope
) {
    private val _models = MutableStateFlow<List<Model>?>(null)
    val healthyModels = _models.asStateFlow()

    init {
        coroutineScope.launch {
            combine(
                AppConfig.Api.OPEN_AI_API_URLS.map {
                    // TODO: open websocket to each URL and get model ids and health status
                    flow<List<Model>> {
                        while (true) {
                            emit(openAI.models())
                            kotlinx.coroutines.delay(1000L)
                        }
                    }
                }
            ) {
                it.reduce { acc, list -> acc + list }
            }.collectLatest { models ->
                _models.update { models.distinct() }
            }
        }
    }
}