package illyan.butler.data.user

import illyan.butler.domain.model.DomainToken
import illyan.butler.domain.model.DomainUser
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    fun getUser(userId: String): Flow<DomainUser?>
    fun getAllUsers(): Flow<List<DomainUser>>
    suspend fun upsertUser(user: DomainUser)
    suspend fun deleteUserData()
    suspend fun deleteUser(userId: String)
    suspend fun refreshUserTokens(userId: String, accessToken: DomainToken?, refreshToken: DomainToken?)
}
