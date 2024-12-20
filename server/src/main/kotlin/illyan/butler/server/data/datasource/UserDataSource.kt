package illyan.butler.server.data.datasource

import illyan.butler.shared.model.identity.UserDto
import kotlinx.coroutines.flow.Flow

interface UserDataSource {
    suspend fun getUser(userId: String): UserDto
    suspend fun getUserByEmailAndPassword(email: String, password: String): UserDto
    suspend fun createUser(user: UserDto): UserDto
    suspend fun editUser(user: UserDto)
    suspend fun deleteUser(userId: String)
    fun getUserChanges(userId: String): Flow<UserDto>
}