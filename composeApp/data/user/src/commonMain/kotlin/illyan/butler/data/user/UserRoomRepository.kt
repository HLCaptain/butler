package illyan.butler.data.user

import illyan.butler.core.local.datasource.UserLocalDataSource
import illyan.butler.domain.model.DomainToken
import illyan.butler.domain.model.DomainUser
import kotlinx.coroutines.flow.Flow
import org.koin.core.annotation.Single

@Single
class UserRoomRepository(
    private val userLocalDataSource: UserLocalDataSource
) : UserRepository {
    override suspend fun upsertUser(user: DomainUser) {
        userLocalDataSource.upsertUser(user)
    }

    override fun getUser(userId: String): Flow<DomainUser?> {
        return userLocalDataSource.getUser(userId)
    }

    override fun getAllUsers(): Flow<List<DomainUser>> {
        return userLocalDataSource.getAllUsers()
    }

    override suspend fun deleteUserData() {
        userLocalDataSource.deleteUserData()
    }

    override suspend fun deleteUser(userId: String) {
        userLocalDataSource.deleteUser(userId)
    }

    override suspend fun refreshUserTokens(userId: String, accessToken: DomainToken?, refreshToken: DomainToken?) {
        userLocalDataSource.refreshUserTokens(userId, accessToken, refreshToken)
    }
}
