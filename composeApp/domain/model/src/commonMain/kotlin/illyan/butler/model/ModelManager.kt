package illyan.butler.model

import illyan.butler.data.model.ModelRepository
import org.koin.core.annotation.Single

@Single
class ModelManager(private val modelRepository: ModelRepository) {
    fun getAvailableModelsFromServer() = modelRepository.getAvailableModelsFromServer()
    fun getAvailableModelsFromProviders() = modelRepository.getAvailableModelsFromProviders()
    val healthyHostCredentials = modelRepository.healthyHostCredentials
}
