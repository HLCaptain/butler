package illyan.butler.core.local.room.datasource

import illyan.butler.core.local.datasource.UserLocalDataSource
import illyan.butler.core.local.room.dao.UserDao
import illyan.butler.core.local.room.mapping.toDomainModel
import illyan.butler.core.local.room.mapping.toRoomModel
import illyan.butler.domain.model.DomainToken
import illyan.butler.domain.model.DomainUser
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Single

@Single
class UserRoomDataSource(
    private val userDao: UserDao
) : UserLocalDataSource {
    override fun getAllUsers(): Flow<List<DomainUser>> {
        return userDao.getUsers().map { users -> users.map { it.toDomainModel() } }
    }

    override fun getUser(userId: String): Flow<DomainUser?> {
        return userDao.getUser(userId).map { it?.toDomainModel() }
    }

    override suspend fun upsertUser(user: DomainUser) {
        userDao.upsertUser(user.toRoomModel())
    }

    override suspend fun deleteUserData() {
        userDao.deleteAllUsers()
    }

    override suspend fun deleteUser(userId: String) {
        userDao.deleteUser(userId)
    }

    override suspend fun refreshUserTokens(userId: String, accessToken: DomainToken?, refreshToken: DomainToken?) {
        userDao.updateTokens(userId, accessToken?.toRoomModel(), refreshToken?.toRoomModel())
    }
}