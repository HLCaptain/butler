package illyan.butler.core.local.room.datasource

import illyan.butler.core.local.datasource.CredentialLocalDataSource
import illyan.butler.core.local.room.dao.ApiKeyCredentialDao
import illyan.butler.core.local.room.mapping.toDomainModel
import illyan.butler.core.local.room.mapping.toRoomModel
import illyan.butler.domain.model.ApiKeyCredential
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Single

@Single
class CredentialRoomDataSource(
    private val apiKeyCredentialDao: ApiKeyCredentialDao
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
}