package illyan.butler.data.user

import illyan.butler.domain.model.User
import illyan.butler.shared.model.chat.Source
import kotlinx.coroutines.flow.Flow
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
interface UserRepository {
    fun getUser(source: Source.Server): Flow<User?>
    fun getAllUsers(): Flow<List<User>>
    suspend fun upsertUser(user: User)
    suspend fun deleteUser(userId: Uuid)
}
