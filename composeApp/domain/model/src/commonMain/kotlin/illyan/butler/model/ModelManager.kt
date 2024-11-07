package illyan.butler.model

import illyan.butler.data.mapping.toDomainModel
import illyan.butler.data.model.ModelRepository
import org.koin.core.annotation.Single

@Single
class ModelManager(
    private val modelRepository: ModelRepository
) {
    suspend fun getAvailableModels() = modelRepository.getAvailableModels().mapKeys { (model, _) -> model.toDomainModel() }
}
