package illyan.butler.server.data.db

import illyan.butler.shared.model.identity.UserDto
import kotlinx.coroutines.flow.Flow
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
interface UserDatabase {
    suspend fun createUser(user: UserDto): UserDto
    suspend fun getUser(userId: Uuid): UserDto
    fun getUserFlow(userId: Uuid): Flow<UserDto>
    suspend fun updateUser(user: UserDto): UserDto
    suspend fun deleteUser(userId: Uuid)
    suspend fun getUserByEmailAndPassword(email: String, password: String): UserDto
    fun getUserByEmailAndPasswordFlow(email: String, password: String): Flow<UserDto>
    suspend fun upsertPasswordForUser(userId: Uuid, password: String)
}
