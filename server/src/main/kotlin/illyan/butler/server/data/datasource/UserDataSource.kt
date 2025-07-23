package illyan.butler.server.data.datasource

import illyan.butler.shared.model.identity.UserDto
import kotlinx.coroutines.flow.Flow
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
interface UserDataSource {
    suspend fun getUser(userId: Uuid): UserDto
    suspend fun getUserByEmailAndPassword(email: String, password: String): UserDto
    suspend fun createUser(user: UserDto): UserDto
    suspend fun editUser(user: UserDto)
    suspend fun deleteUser(userId: Uuid)
    fun getUserChanges(userId: Uuid): Flow<UserDto>
}
