package illyan.butler.data.user

import illyan.butler.domain.model.Token
import illyan.butler.domain.model.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import org.koin.core.annotation.Single
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
@Single
class UserMemoryRepository : UserRepository {
    private val users = MutableStateFlow<List<User>>(listOf())

    override suspend fun upsertUser(user: User) {
        users.update { currentUsers ->
            currentUsers.filterNot { it.id == user.id } + user
        }
    }

    override fun getUser(userId: Uuid): Flow<User?> {
        return users.map { users ->
            users.firstOrNull { it.id == userId }
        }
    }

    override fun getAllUsers(): Flow<List<User>> {
        return users.asStateFlow()
    }

    override suspend fun deleteUserData() {
        users.update { emptyList() }
    }

    override suspend fun deleteUser(userId: Uuid) {
        users.update { currentUsers ->
            currentUsers.filterNot { it.id == userId }
        }
    }

    override suspend fun refreshUserTokens(
        userId: Uuid,
        accessToken: Token?,
        refreshToken: Token?
    ) {
        // Mock implementation: No-op
    }
}