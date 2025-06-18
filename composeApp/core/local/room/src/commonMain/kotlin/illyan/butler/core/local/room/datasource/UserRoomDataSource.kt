package illyan.butler.core.local.room.datasource

import illyan.butler.core.local.datasource.UserLocalDataSource
import illyan.butler.core.local.room.dao.UserDao
import illyan.butler.core.local.room.mapping.toDomainModel
import illyan.butler.core.local.room.mapping.toRoomModel
import illyan.butler.domain.model.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Single
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
@Single
class UserRoomDataSource(
    private val userDao: UserDao
) : UserLocalDataSource {
    override fun getAllUsers(): Flow<List<User>> {
        return userDao.getUsers().map { users -> users.map { it.toDomainModel() } }
    }

    override fun getUser(userId: Uuid): Flow<User?> {
        return userDao.getUser(userId.toString()).map { it?.toDomainModel() }
    }

    override suspend fun upsertUser(user: User) {
        userDao.upsertUser(user.toRoomModel())
    }

    override suspend fun deleteUserData() {
        userDao.deleteAllUsers()
    }

    override suspend fun deleteUser(userId: Uuid) {
        userDao.deleteUser(userId.toString())
    }
}