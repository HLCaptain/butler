package illyan.butler.model

import illyan.butler.data.model.ModelRepository
import org.koin.core.annotation.Single

@Single
class ModelManager(private val modelRepository: ModelRepository) {
    fun getAiSources() = modelRepository.getAiSources()
    val healthyHostCredentials = modelRepository.healthyHostCredentials
}
