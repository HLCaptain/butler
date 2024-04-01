package illyan.butler.api_gateway.data.service

import illyan.butler.api_gateway.data.model.ai.ModelDto
import illyan.butler.api_gateway.utils.AppConfig
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import org.koin.core.annotation.Single

@Single
class AIService(private val client: HttpClient) {
    suspend fun getAIModels() = client.get("${AppConfig.Api.AI_API_URL}/models").body<List<ModelDto>>()
    suspend fun getAIModel(modelId: String) = client.get("${AppConfig.Api.AI_API_URL}/models/$modelId").body<ModelDto>()
}
