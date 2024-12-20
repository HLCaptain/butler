package illyan.butler.data.credential

import illyan.butler.domain.model.ApiKeyCredential
import kotlinx.coroutines.flow.Flow

interface CredentialRepository {
    val apiKeyCredentials: Flow<List<ApiKeyCredential>?>

    suspend fun upsertApiKeyCredential(apiKeyCredential: ApiKeyCredential)
    suspend fun deleteApiKeyCredentialByUrl(providerUrl: String)
}
