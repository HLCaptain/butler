package illyan.butler.services.identity.data.db

import illyan.butler.services.identity.data.model.identity.UserDto

interface UserDatabase {
    suspend fun createUser(user: UserDto): UserDto
    suspend fun getUser(userId: String): UserDto
    suspend fun updateUser(user: UserDto): UserDto
    suspend fun deleteUser(userId: String)
    suspend fun getUserIdByEmailAndPassword(email: String, password: String): String
    suspend fun upsertPasswordForUser(userId: String, password: String)
}