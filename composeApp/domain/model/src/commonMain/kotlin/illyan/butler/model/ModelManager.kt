package illyan.butler.model

import illyan.butler.data.model.ModelRepository
import illyan.butler.domain.model.DomainModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import org.koin.core.annotation.Single

@Single
class ModelManager(
    private val modelRepository: ModelRepository
) {
    fun getAvailableModels(): Flow<List<DomainModel>> {
        return combine(
            modelRepository.getAvailableModelsFromServer(),
            modelRepository.getAvailableModelsFromProviders()
        ) { serverModels, providerModels ->
            serverModels + providerModels
        }
    }
}
