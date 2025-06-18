package illyan.butler.data.credential

import illyan.butler.core.local.datasource.CredentialLocalDataSource
import illyan.butler.domain.model.UserTokens
import illyan.butler.shared.model.auth.ApiKeyCredential
import kotlinx.coroutines.flow.Flow
import org.koin.core.annotation.Single
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
@Single
class CredentialLocalRepository(
    private val credentialLocalDataSource: CredentialLocalDataSource,
) : CredentialRepository {
    override val apiKeyCredentials: Flow<List<ApiKeyCredential>?>
        get() = credentialLocalDataSource.getCredentials()

    override suspend fun upsertApiKeyCredential(apiKeyCredential: ApiKeyCredential) {
        credentialLocalDataSource.upsertCredential(apiKeyCredential)
    }

    override suspend fun deleteApiKeyCredentialByUrl(providerUrl: String) {
        credentialLocalDataSource.deleteCredential(providerUrl)
    }

    override suspend fun upsertUserTokens(
        userId: Uuid,
        userTokens: UserTokens
    ) {
        credentialLocalDataSource.upsertUserTokens(userId, userTokens)
    }
}
