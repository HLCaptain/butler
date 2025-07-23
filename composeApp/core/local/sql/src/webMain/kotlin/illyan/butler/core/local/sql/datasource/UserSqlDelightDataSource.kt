package illyan.butler.core.local.sql.datasource

import illyan.butler.core.local.datasource.UserLocalDataSource
import illyan.butler.core.local.sql.DatabaseHelper
import illyan.butler.core.local.sql.mapping.toDomainModel
import illyan.butler.core.local.sql.mapping.toSqlDelightModel
import illyan.butler.core.local.sqldelight.db.ButlerDatabase
import illyan.butler.domain.model.User
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Single
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
@Single
class UserSqlDelightDataSource(
    private val databaseHelper: DatabaseHelper<ButlerDatabase>
) : UserLocalDataSource {

    override fun getAllUsers(): Flow<List<User>> {
        Napier.d { "Getting all users" }
        return databaseHelper.queryAsListFlow {
            userQueries.selectAllUsers()
        }.map { users -> users.map { it.toDomainModel() } }
    }

    override fun getUser(userId: Uuid): Flow<User?> {
        Napier.d { "Getting user with id: $userId" }
        return databaseHelper.queryAsOneOrNullFlow {
            userQueries.selectUserById(userId.toString())
        }.map { it?.toDomainModel() }
    }

    override suspend fun upsertUser(user: User) {
        Napier.d { "Upserting user with id: ${user.id}" }
        val sqlDelightUser = user.toSqlDelightModel()
        databaseHelper.withDatabase {
            userQueries.insertOrReplaceUser(
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
    }

    override suspend fun deleteUserData() {
        Napier.d { "Deleting all user data" }
        databaseHelper.withDatabase {
            userQueries.deleteAllUsers()
        }
    }

    override suspend fun deleteUser(userId: Uuid) {
        Napier.d { "Deleting user with id: $userId" }
        databaseHelper.withDatabase {
            userQueries.deleteUser(userId.toString())
        }
    }
}
