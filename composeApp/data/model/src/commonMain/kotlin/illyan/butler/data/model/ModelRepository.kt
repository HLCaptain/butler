package illyan.butler.data.model

import illyan.butler.shared.model.auth.ApiKeyCredential
import illyan.butler.shared.model.chat.AiSource
import kotlinx.coroutines.flow.Flow

interface ModelRepository {
    fun getAiSources(): Flow<List<AiSource>>
    val healthyHostCredentials: Flow<List<ApiKeyCredential>>
}
