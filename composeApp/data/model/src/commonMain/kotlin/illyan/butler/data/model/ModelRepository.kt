package illyan.butler.data.model

import illyan.butler.domain.model.DomainModel
import illyan.butler.shared.model.auth.ApiKeyCredential
import kotlinx.coroutines.flow.Flow

interface ModelRepository {
    fun getAvailableModelsFromServer(): Flow<List<DomainModel>>
    fun getAvailableModelsFromProviders(): Flow<List<DomainModel>>
    val healthyHostCredentials: Flow<List<ApiKeyCredential>>
}
