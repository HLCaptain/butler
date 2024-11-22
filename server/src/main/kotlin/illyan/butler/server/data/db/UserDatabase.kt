package illyan.butler.server.data.db

import illyan.butler.shared.model.identity.UserDto

interface UserDatabase {
    suspend fun createUser(user: UserDto): UserDto
    suspend fun getUser(userId: String): UserDto
    suspend fun updateUser(user: UserDto): UserDto
    suspend fun deleteUser(userId: String)
    suspend fun getUserByEmailAndPassword(email: String, password: String): UserDto
    suspend fun upsertPasswordForUser(userId: String, password: String)
}