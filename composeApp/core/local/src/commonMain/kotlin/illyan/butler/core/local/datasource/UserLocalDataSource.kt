package illyan.butler.core.local.datasource

import illyan.butler.domain.model.DomainToken
import illyan.butler.domain.model.DomainUser
import kotlinx.coroutines.flow.Flow

interface UserLocalDataSource {
    fun getAllUsers(): Flow<List<DomainUser>>
    fun getUser(userId: String): Flow<DomainUser?>
    suspend fun upsertUser(user: DomainUser)
    suspend fun deleteUserData()
    suspend fun deleteUser(userId: String)
    suspend fun refreshUserTokens(userId: String, accessToken: DomainToken?, refreshToken: DomainToken?)
}
