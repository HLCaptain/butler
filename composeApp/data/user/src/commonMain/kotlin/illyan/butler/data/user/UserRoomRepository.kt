package illyan.butler.data.user

import illyan.butler.core.local.datasource.UserLocalDataSource
import illyan.butler.domain.model.Token
import illyan.butler.domain.model.User
import kotlinx.coroutines.flow.Flow
import org.koin.core.annotation.Single
import kotlin.uuid.Uuid

@Single
class UserRoomRepository(
    private val userLocalDataSource: UserLocalDataSource
) : UserRepository {
    override suspend fun upsertUser(user: User) {
        userLocalDataSource.upsertUser(user)
    }

    override fun getUser(userId: Uuid): Flow<User?> {
        return userLocalDataSource.getUser(userId)
    }

    override fun getAllUsers(): Flow<List<User>> {
        return userLocalDataSource.getAllUsers()
    }

    override suspend fun deleteUserData() {
        userLocalDataSource.deleteUserData()
    }

    override suspend fun deleteUser(userId: Uuid) {
        userLocalDataSource.deleteUser(userId)
    }

    override suspend fun refreshUserTokens(userId: Uuid, accessToken: Token?, refreshToken: Token?) {
        userLocalDataSource.refreshUserTokens(userId, accessToken, refreshToken)
    }
}
