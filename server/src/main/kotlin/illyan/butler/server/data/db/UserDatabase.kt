package illyan.butler.server.data.db

import illyan.butler.shared.model.identity.UserDto
import kotlinx.coroutines.flow.Flow

interface UserDatabase {
    suspend fun createUser(user: UserDto): UserDto
    suspend fun getUser(userId: String): UserDto
    fun getUserFlow(userId: String): Flow<UserDto>
    suspend fun updateUser(user: UserDto): UserDto
    suspend fun deleteUser(userId: String)
    suspend fun getUserByEmailAndPassword(email: String, password: String): UserDto
    fun getUserByEmailAndPasswordFlow(email: String, password: String): Flow<UserDto>
    suspend fun upsertPasswordForUser(userId: String, password: String)
}
