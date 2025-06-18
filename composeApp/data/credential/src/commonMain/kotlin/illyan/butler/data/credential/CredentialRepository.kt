package illyan.butler.data.credential

import illyan.butler.domain.model.UserTokens
import illyan.butler.shared.model.auth.ApiKeyCredential
import kotlinx.coroutines.flow.Flow
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
interface CredentialRepository {
    val apiKeyCredentials: Flow<List<ApiKeyCredential>?>

    suspend fun upsertApiKeyCredential(apiKeyCredential: ApiKeyCredential)
    suspend fun deleteApiKeyCredentialByUrl(providerUrl: String)
    suspend fun upsertUserTokens(userId: Uuid, userTokens: UserTokens)
}
