package illyan.butler.services.identity.data.service

import illyan.butler.services.identity.data.cache.UserCache
import illyan.butler.services.identity.data.datasource.UserDataSource
import illyan.butler.services.identity.data.db.UserDatabase
import illyan.butler.services.identity.data.model.identity.UserDto
import kotlinx.coroutines.flow.Flow
import org.koin.core.annotation.Single

@Single
class IdentityService(
    private val userCache: UserCache,
    private val userDatabase: UserDatabase
) : UserDataSource {
    override suspend fun getUser(userId: String): UserDto {
        return userCache.getUser(userId) ?: userDatabase.getUser(userId).also {
            userCache.setUser(it)
        }
    }

    override suspend fun getUserIdByEmailAndPassword(email: String, password: String): String {
        return userDatabase.getUserIdByEmailAndPassword(email, password)
    }

    override suspend fun createUser(user: UserDto): UserDto {
        return userDatabase.createUser(user).also {
            userCache.setUser(it)
        }
    }

    override suspend fun editUser(user: UserDto) {
        userDatabase.updateUser(user)
        userCache.setUser(user)
    }

    override suspend fun deleteUser(userId: String) {
        return userDatabase.deleteUser(userId).also {
            userCache.deleteUser(userId)
        }
    }

    override fun getUserChanges(userId: String): Flow<UserDto> {
        return userCache.getUserChanges(userId)
    }

    suspend fun registerUser(username: String, email: String, password: String): UserDto {
        return createUser(UserDto(null, email, username)).also {
            userDatabase.upsertPasswordForUser(it.id!!, password)
        }
    }
}