package illyan.butler.manager

import illyan.butler.data.mapping.toDomainModel
import illyan.butler.repository.ModelRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Single

@Single
class ModelManager(
    private val modelRepository: ModelRepository
) {
    suspend fun getAvailableModels() = modelRepository.getAvailableModels().map { it.toDomainModel() }
}
