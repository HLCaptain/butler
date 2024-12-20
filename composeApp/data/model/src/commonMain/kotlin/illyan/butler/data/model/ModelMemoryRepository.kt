package illyan.butler.data.model

import illyan.butler.domain.model.DomainModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.koin.core.annotation.Single

@Single
class ModelMemoryRepository : ModelRepository {
    private val availableModelsFromServer = listOf(
        DomainModel(
            name = "Model 1",
            id = "model1",
            ownedBy = "illyan",
            endpoint = "http://localhost:8080",
        ),
        DomainModel(
            name = "Model 2",
            id = "model2",
            ownedBy = "illyan",
            endpoint = "http://localhost:8080",
        ),
        DomainModel(
            name = "Model 3",
            id = "model3",
            ownedBy = "illyan",
            endpoint = "http://localhost:8080",
        )
    )
    private val availableModelsFromProviders = listOf(
        DomainModel(
            name = "GPT-o3",
            id = "gpt-o3",
            ownedBy = "illyan",
            endpoint = "http://localhost:8080",
        ),
        DomainModel(
            name = "TTS-3",
            id = "tts-3",
            ownedBy = "illyan",
            endpoint = "http://localhost:8080",
        ),
        DomainModel(
            name = "GPT-6o",
            id = "gpt-6o",
            ownedBy = "illyan",
            endpoint = "http://localhost:8080",
        )
    )
    override fun getAvailableModelsFromServer(): Flow<List<DomainModel>> = flowOf(availableModelsFromServer)
    override fun getAvailableModelsFromProviders(): Flow<List<DomainModel>> = flowOf(availableModelsFromProviders)
}
