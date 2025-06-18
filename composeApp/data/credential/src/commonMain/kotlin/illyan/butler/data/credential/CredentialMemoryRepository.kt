package illyan.butler.data.credential

import illyan.butler.domain.model.UserTokens
import illyan.butler.shared.model.auth.ApiKeyCredential
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import org.koin.core.annotation.Single
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
@Single
class CredentialMemoryRepository : CredentialRepository {
    override val apiKeyCredentials = MutableStateFlow(listOf<ApiKeyCredential>())

    override suspend fun upsertApiKeyCredential(apiKeyCredential: ApiKeyCredential) {
        apiKeyCredentials.update { it + apiKeyCredential }
    }

    override suspend fun deleteApiKeyCredentialByUrl(providerUrl: String) {
        apiKeyCredentials.update { credentials -> credentials.filter { it.providerUrl != providerUrl } }
    }

    override suspend fun upsertUserTokens(
        userId: Uuid,
        userTokens: UserTokens
    ) {
        // No-op in memory repository
    }
}