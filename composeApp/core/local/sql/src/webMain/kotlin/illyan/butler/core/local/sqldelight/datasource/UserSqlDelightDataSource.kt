package illyan.butler.core.local.sqldelight.datasource

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import illyan.butler.core.local.datasource.UserLocalDataSource
import illyan.butler.core.local.sqldelight.db.ButlerDatabase
import illyan.butler.core.local.sqldelight.mapping.toDomainModel
import illyan.butler.core.local.sqldelight.mapping.toSqlDelightModel
import illyan.butler.domain.model.User
import io.github.aakira.napier.Napier
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Single
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
@Single
class UserSqlDelightDataSource(
    private val database: ButlerDatabase
) : UserLocalDataSource {

    override fun getAllUsers(): Flow<List<User>> {
        Napier.d { "Getting all users" }
        return database.userQueries.selectAllUsers()
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { users -> users.map { it.toDomainModel() } }
    }

    override fun getUser(userId: Uuid): Flow<User?> {
        Napier.d { "Getting user with id: $userId" }
        return database.userQueries.selectUserById(userId.toString())
            .asFlow()
            .mapToOneOrNull(Dispatchers.Default)
            .map { it?.toDomainModel() }
    }

    override suspend fun upsertUser(user: User) {
        Napier.d { "Upserting user with id: ${user.id}" }
        val sqlDelightUser = user.toSqlDelightModel()
        database.userQueries.insertOrReplaceUser(
            id = sqlDelightUser.id,
            endpoint = sqlDelightUser.endpoint,
            email = sqlDelightUser.email,
            username = sqlDelightUser.username,
            displayName = sqlDelightUser.displayName,
            phone = sqlDelightUser.phone,
            fullName = sqlDelightUser.fullName,
            photoUrl = sqlDelightUser.photoUrl,
            address = sqlDelightUser.address,
            filterOptions = sqlDelightUser.filterOptions,
            customPrompts = sqlDelightUser.customPrompts
        )
    }

    override suspend fun deleteUserData() {
        Napier.d { "Deleting all user data" }
        database.userQueries.deleteAllUsers()
    }

    override suspend fun deleteUser(userId: Uuid) {
        Napier.d { "Deleting user with id: $userId" }
        database.userQueries.deleteUser(userId.toString())
    }
}
