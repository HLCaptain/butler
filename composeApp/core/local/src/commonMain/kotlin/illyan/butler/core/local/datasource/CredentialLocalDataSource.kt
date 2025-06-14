package illyan.butler.core.local.datasource

import illyan.butler.shared.model.auth.ApiKeyCredential
import kotlinx.coroutines.flow.Flow

interface CredentialLocalDataSource {
    fun getCredentials(): Flow<List<ApiKeyCredential>>
    suspend fun upsertCredential(credential: ApiKeyCredential)
    suspend fun deleteCredential(providerUrl: String)
    suspend fun deleteAllCredentials()
}
