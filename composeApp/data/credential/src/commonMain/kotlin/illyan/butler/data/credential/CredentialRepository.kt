package illyan.butler.data.credential

import illyan.butler.shared.model.auth.ApiKeyCredential
import kotlinx.coroutines.flow.Flow

interface CredentialRepository {
    val apiKeyCredentials: Flow<List<ApiKeyCredential>?>

    suspend fun upsertApiKeyCredential(apiKeyCredential: ApiKeyCredential)
    suspend fun deleteApiKeyCredentialByUrl(providerUrl: String)
}
