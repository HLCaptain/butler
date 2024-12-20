package illyan.butler.data.user

import illyan.butler.core.local.datasource.UserLocalDataSource
import illyan.butler.domain.model.DomainToken
import illyan.butler.domain.model.DomainUser
import kotlinx.coroutines.flow.Flow
import org.koin.core.annotation.Single

// TODO: should be using Store5 implementation
@Single
class UserRoomRepository(
    private val userLocalDataSource: UserLocalDataSource
) : UserRepository {
    override suspend fun upsertUser(user: DomainUser) {
        TODO("Not yet implemented")
    }

    override fun getUser(userId: String): Flow<DomainUser?> {
        TODO("Not yet implemented")
    }

    override fun getAllUsers(): Flow<List<DomainUser>> {
        TODO("Not yet implemented")
    }

    override suspend fun deleteUserData() {
        TODO("Not yet implemented")
    }

    override suspend fun deleteUserData(userId: String) {
        TODO("Not yet implemented")
    }

    override suspend fun refreshUserTokens(userId: String, accessToken: DomainToken?, refreshToken: DomainToken?) {
        userLocalDataSource.refreshUserTokens(userId, accessToken, refreshToken)
    }
}
