package illyan.butler.core.local.room.datasource

import illyan.butler.core.local.datasource.CredentialLocalDataSource
import illyan.butler.core.local.room.dao.ApiKeyCredentialDao
import illyan.butler.core.local.room.dao.UserTokensDao
import illyan.butler.core.local.room.mapping.toDomainModel
import illyan.butler.core.local.room.mapping.toRoomModel
import illyan.butler.domain.model.UserTokens
import illyan.butler.shared.model.auth.ApiKeyCredential
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Single
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
@Single
class CredentialRoomDataSource(
    private val apiKeyCredentialDao: ApiKeyCredentialDao,
    private val userTokensDao: UserTokensDao,
) : CredentialLocalDataSource {
    override fun getCredentials(): Flow<List<ApiKeyCredential>> {
        return apiKeyCredentialDao.getAllApiKeyCredentials().map { keys -> keys.map { it.toDomainModel() } }
    }

    override suspend fun upsertCredential(credential: ApiKeyCredential) {
        apiKeyCredentialDao.upsertApiKeyCredential(credential.toRoomModel())
    }

    override suspend fun deleteCredential(providerUrl: String) {
        apiKeyCredentialDao.deleteApiKeyCredentialsByProviderUrl(providerUrl)
    }

    override suspend fun deleteAllCredentials() {
        apiKeyCredentialDao.deleteAllApiKeyCredentials()
    }

    override fun getUserTokens(userId: Uuid): Flow<UserTokens?> {
        return userTokensDao.getUserTokensByUserId(userId.toString()).map { it?.toDomainModel() }
    }

    override suspend fun upsertUserTokens(
        userId: Uuid,
        tokens: UserTokens
    ) {
        userTokensDao.upsertUserTokens(tokens.toRoomModel(userId.toString()))
    }
}
