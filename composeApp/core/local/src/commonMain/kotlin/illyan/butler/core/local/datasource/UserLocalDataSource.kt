package illyan.butler.core.local.datasource

import illyan.butler.domain.model.User
import kotlinx.coroutines.flow.Flow
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
interface UserLocalDataSource {
    fun getAllUsers(): Flow<List<User>>
    fun getUser(userId: Uuid): Flow<User?>
    suspend fun upsertUser(user: User)
    suspend fun deleteUserData()
    suspend fun deleteUser(userId: Uuid)
}
