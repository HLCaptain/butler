package illyan.butler.shared.llm

import com.aallam.openai.client.OpenAI
import com.aallam.openai.client.OpenAIHost
import illyan.butler.shared.model.llm.ModelDto
import io.github.aakira.napier.Napier
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlin.time.Duration

@OptIn(ExperimentalCoroutinesApi::class)
fun Flow<Map<String, String>>.mapToModels(pingDuration: Duration) = flatMapLatest { credentials ->
    credentials.mapToModels(pingDuration)
}

fun Map<String, String>.mapToModels(pingDuration: Duration) = combine(*map { (url, apiKey) ->
    flow {
        emit(null)
        while (true) {
            try {
                emit(OpenAI(
                    token = apiKey,
                    host = OpenAIHost(url)
                ).models().map { ModelDto(
                    id = it.id.id,
                    name = null,
                    endpoint = url,
                    ownedBy = it.ownedBy,
                ) })
            } catch (e: Exception) {
                Napier.e(e) { "Error fetching models from $url" }
                emit(emptyList())
            }
            delay(pingDuration)
        }
    }.flowOn(Dispatchers.IO)
}.toTypedArray()) { it.toList().mapNotNull { it }.flatten() }
