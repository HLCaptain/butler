package illyan.butler.manager

import illyan.butler.data.mapping.toDomainModel
import illyan.butler.repository.ModelRepository
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Single

@Single
class ModelManager(
    modelRepository: ModelRepository
) {
    val availableModels = modelRepository.getAvailableModels()
        .map { models -> models.map { it.toDomainModel() } }
}
