package illyan.butler.data.user

import illyan.butler.domain.model.DomainToken
import illyan.butler.domain.model.DomainUser
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import org.koin.core.annotation.Single

@Single
class UserMemoryRepository : UserRepository {
    private val users = MutableStateFlow<List<DomainUser>>(listOf())

    override suspend fun upsertUser(user: DomainUser) {
        users.update { currentUsers ->
            currentUsers.filterNot { it.id == user.id } + user
        }
    }

    override fun getUser(userId: String): Flow<DomainUser?> {
        return users.map { users ->
            users.firstOrNull { it.id == userId }
        }
    }

    override fun getAllUsers(): Flow<List<DomainUser>> {
        return users.asStateFlow()
    }

    override suspend fun deleteUserData() {
        users.update { emptyList() }
    }

    override suspend fun deleteUserData(userId: String) {
        users.update { currentUsers ->
            currentUsers.filterNot { it.id == userId }
        }
    }

    override suspend fun refreshUserTokens(
        userId: String,
        accessToken: DomainToken?,
        refreshToken: DomainToken?
    ) {
        // Mock implementation: No-op
    }
}