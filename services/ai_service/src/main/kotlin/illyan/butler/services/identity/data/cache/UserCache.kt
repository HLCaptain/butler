package illyan.butler.services.identity.data.cache

import illyan.butler.services.identity.data.model.identity.UserDto
import kotlinx.coroutines.flow.Flow

interface UserCache {
    suspend fun getUser(userId: String): UserDto?
    fun getUserChanges(userId: String): Flow<UserDto>
    suspend fun setUser(user: UserDto): UserDto
    suspend fun deleteUser(userId: String)
}