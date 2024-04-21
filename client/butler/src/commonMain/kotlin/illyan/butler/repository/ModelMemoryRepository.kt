package illyan.butler.repository

import illyan.butler.data.network.datasource.ModelNetworkDataSource
import illyan.butler.data.network.model.ai.ModelDto
import org.koin.core.annotation.Single

@Single
class ModelMemoryRepository(
    private val modelNetworkDataSource: ModelNetworkDataSource
) : ModelRepository {
    private val availableModels = listOf<ModelDto>(
        ModelDto(
            name = "Model 1",
            id = "model1",
            description = "Model 1 description",
            author = "illyan",
            greetingMessage = "Hello, I am model 1",
        ),
        ModelDto(
            name = "Model 2",
            id = "model2",
            description = "Model 2 description",
            author = "illyan",
            greetingMessage = "Hello, I am model 2",
        ),
        ModelDto(
            name = "Model 3",
            id = "model3",
            description = "Model 3 description",
            author = "illyan",
            greetingMessage = "Hello, I am model 3",
        ),
    )
    override suspend fun getAvailableModels(): List<ModelDto> = availableModels
}
