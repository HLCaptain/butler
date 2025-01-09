package illyan.butler.data.model

import illyan.butler.domain.model.ApiKeyCredential
import illyan.butler.domain.model.DomainModel
import kotlinx.coroutines.flow.Flow

interface ModelRepository {
    fun getAvailableModelsFromServer(): Flow<List<DomainModel>>
    fun getAvailableModelsFromProviders(): Flow<List<DomainModel>>
    val healthyHostCredentials: Flow<List<ApiKeyCredential>>
}
