package illyan.butler.data.model

import illyan.butler.shared.model.auth.ApiKeyCredential
import illyan.butler.shared.model.chat.AiSource
import illyan.butler.shared.model.chat.ApiType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.koin.core.annotation.Single

@Single
class ModelMemoryRepository : ModelRepository {
    private val availableModelsFromServer = listOf(
        AiSource.Server(
            source = AiSource.Api(
                modelId = "gpt-3.5-turbo",
                endpoint = "http://localhost:8080",
                apiType = ApiType.OPENAI
            )
        ),
        AiSource.Server(
            source = AiSource.Api(
                modelId = "gpt-4",
                endpoint = "http://localhost:8080",
                apiType = ApiType.OPENAI
            )
        ),
        AiSource.Server(
            source = AiSource.Api(
                modelId = "gpt-4o",
                endpoint = "http://localhost:8080",
                apiType = ApiType.OPENAI
            )
        ),
    )
    private val availableModelsFromProviders = listOf(
        AiSource.Api(
            modelId = "gpt-3.5-turbo",
            endpoint = "https://api.openai.com/v1/",
            apiType = ApiType.OPENAI
        ),
        AiSource.Api(
            modelId = "gpt-4",
            endpoint = "https://api.openai.com/v1/",
            apiType = ApiType.OPENAI
        ),
        AiSource.Api(
            modelId = "gpt-4o",
            endpoint = "https://api.openai.com/v1/",
            apiType = ApiType.OPENAI
        ),
    )
    override fun getAiSources(): Flow<List<AiSource>> = flowOf(availableModelsFromServer + availableModelsFromProviders)
    override val healthyHostCredentials: Flow<List<ApiKeyCredential>>
        get() = flowOf(emptyList())
}
