package illyan.butler.repository.model

import illyan.butler.data.network.model.ai.ModelDto

interface ModelRepository {
    suspend fun getAvailableModels(): Map<ModelDto, List<String>>
}
