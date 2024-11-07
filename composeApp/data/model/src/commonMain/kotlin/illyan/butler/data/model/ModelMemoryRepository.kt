package illyan.butler.data.model

import illyan.butler.domain.model.DomainModel
import illyan.butler.shared.model.llm.ModelDto
import org.koin.core.annotation.Single

@Single
class ModelMemoryRepository : ModelRepository {
    private val availableModels = mapOf(
        DomainModel(
            name = "Model 1",
            id = "model1",
            description = "Model 1 description",
            author = "illyan",
            greetingMessage = "Hello, I am model 1",
        ) to listOf("provider1", "provider2"),
        DomainModel(
            name = "Model 2",
            id = "model2",
            description = "Model 2 description",
            author = "illyan",
            greetingMessage = "Hello, I am model 2",
        ) to listOf("provider1", "provider2"),
        DomainModel(
            name = "Model 3",
            id = "model3",
            description = "Model 3 description",
            author = "illyan",
            greetingMessage = "Hello, I am model 3",
        ) to listOf("provider2", "provider3")
    )
    override suspend fun getAvailableModels(): Map<DomainModel, List<String>> = availableModels
}
