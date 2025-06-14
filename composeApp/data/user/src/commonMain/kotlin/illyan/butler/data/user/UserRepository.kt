package illyan.butler.data.user

import illyan.butler.domain.model.Token
import illyan.butler.domain.model.User
import kotlinx.coroutines.flow.Flow
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
interface UserRepository {
    fun getUser(userId: Uuid): Flow<User?>
    fun getAllUsers(): Flow<List<User>>
    suspend fun upsertUser(user: User)
    suspend fun deleteUserData()
    suspend fun deleteUser(userId: Uuid)
    suspend fun refreshUserTokens(userId: Uuid, accessToken: Token?, refreshToken: Token?)
}
