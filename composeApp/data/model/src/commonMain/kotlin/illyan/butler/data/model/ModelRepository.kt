package illyan.butler.data.model

import illyan.butler.domain.model.DomainModel

interface ModelRepository {
    suspend fun getAvailableModels(): Map<DomainModel, List<String>>
}
