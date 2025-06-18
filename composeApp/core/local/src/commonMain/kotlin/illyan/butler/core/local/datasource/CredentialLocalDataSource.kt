package illyan.butler.core.local.datasource

import illyan.butler.domain.model.UserTokens
import illyan.butler.shared.model.auth.ApiKeyCredential
import kotlinx.coroutines.flow.Flow
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
interface CredentialLocalDataSource {
    fun getCredentials(): Flow<List<ApiKeyCredential>>
    suspend fun upsertCredential(credential: ApiKeyCredential)
    suspend fun deleteCredential(providerUrl: String)
    suspend fun deleteAllCredentials()
    fun getUserTokens(userId: Uuid): Flow<UserTokens?>
    suspend fun upsertUserTokens(userId: Uuid, tokens: UserTokens)
}
