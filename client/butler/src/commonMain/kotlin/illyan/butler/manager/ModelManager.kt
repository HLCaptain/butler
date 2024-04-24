package illyan.butler.manager

import illyan.butler.data.mapping.toDomainModel
import illyan.butler.repository.ModelRepository
import org.koin.core.annotation.Single

@Single
class ModelManager(
    private val modelRepository: ModelRepository
) {
    suspend fun getAvailableModels() = modelRepository.getAvailableModels().mapKeys { (model, _) -> model.toDomainModel() }
}
