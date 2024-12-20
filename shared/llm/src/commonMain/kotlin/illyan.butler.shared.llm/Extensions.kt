package illyan.butler.shared.llm

import com.aallam.openai.client.OpenAI
import com.aallam.openai.client.OpenAIHost
import illyan.butler.shared.llm.model.Model
import io.github.aakira.napier.Napier
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlin.time.Duration

@OptIn(ExperimentalCoroutinesApi::class)
fun Flow<List<Pair<String, String>>>.mapToProvidedModels(pingDuration: Duration) = flatMapLatest { credentials ->
    combine(*credentials.map { (url, apiKey) ->
        flow {
            emit(url to null)
            while (true) {
                try {
                    emit(url to OpenAI(
                        token = apiKey,
                        host = OpenAIHost(url)
                    ).models())
                } catch (e: Exception) {
                    Napier.e(e) { "Error fetching models from $url" }
                    emit(url to emptyList())
                }
                delay(pingDuration)
            }
        }
    }.toTypedArray()) { it.toList() }
}

fun Flow<List<Pair<String, List<Model>?>>>.mapToModelsAndProviders() = map { models ->
    models.flatMap { (provider, models) ->
        (models ?: emptyList()).map { it to provider }
    }.groupBy { it.first.id }.map { (_, modelWithProvider) ->
        modelWithProvider.first().first to modelWithProvider.map { it.second }
    }.toMap() // Map<ModelDto, List<String>> where key is model and value is list of providers
}
