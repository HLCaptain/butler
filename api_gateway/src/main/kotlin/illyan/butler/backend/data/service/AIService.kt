package illyan.butler.backend.data.service

import illyan.butler.backend.AppConfig
import illyan.butler.backend.data.model.ai.ModelDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import org.koin.core.annotation.Single

@Single
class AIService(private val client: HttpClient) {
    suspend fun getAIModels() = client.get("${AppConfig.Api.AI_API_URL}/models").body<Map<ModelDto, List<String>>>()
    suspend fun getAIModel(modelId: String) = client.get("${AppConfig.Api.AI_API_URL}/models/$modelId").body<Pair<ModelDto, List<String>>>()
    suspend fun getAIModelProviders() = client.get("${AppConfig.Api.AI_API_URL}/providers").body<List<String>>()
}
