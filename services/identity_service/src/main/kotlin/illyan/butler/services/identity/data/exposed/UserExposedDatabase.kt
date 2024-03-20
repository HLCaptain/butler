package illyan.butler.services.identity.data.exposed

import illyan.butler.services.identity.data.db.UserDatabase
import illyan.butler.services.identity.data.model.identity.AddressDto
import illyan.butler.services.identity.data.model.identity.UserDto
import illyan.butler.services.identity.data.schema.UserPasswords
import illyan.butler.services.identity.data.schema.Users
import kotlinx.coroutines.CoroutineDispatcher
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import org.koin.core.annotation.Single

@Single
class UserExposedDatabase(
    private val database: Database,
    private val dispatcher: CoroutineDispatcher
) : UserDatabase {
    init {
        transaction(database) {
            SchemaUtils.create(Users, UserPasswords)
        }
    }

    override suspend fun createUser(user: UserDto): UserDto {
        return newSuspendedTransaction(dispatcher, database) {
            val userId = Users.insertAndGetId { setUser(it, user) }
            user.copy(id = userId.value)
        }
    }

    override suspend fun getUser(userId: String): UserDto {
        return newSuspendedTransaction(dispatcher, database) {
            Users.selectAll().where { Users.id eq userId }.first().toUserDto()
        }
    }

    override suspend fun updateUser(user: UserDto): UserDto {
        return newSuspendedTransaction(dispatcher, database) {
            val isUserUpdated = Users.update({ Users.id eq user.id!! }) { setUser(it, user) } > 0
            if (isUserUpdated) user else throw Exception("User not found")
        }
    }

    override suspend fun deleteUser(userId: String) {
        newSuspendedTransaction(dispatcher, database) {
            Users.deleteWhere { Users.id eq userId }
        }
    }

    override suspend fun getUserIdByEmailAndPassword(email: String, password: String): String {
        return newSuspendedTransaction(dispatcher, database) {
            val userId = Users.selectAll().where { Users.email eq email }.first().toUserDto().id ?: throw Exception("User not found")
            UserPasswords.selectAll().where { UserPasswords.userId eq userId }.first().let {
                if (it[UserPasswords.passwordHash] == password) userId else throw Exception("User not found")
            }
        }
    }

    override suspend fun upsertPasswordForUser(userId: String, password: String) {
        return newSuspendedTransaction(dispatcher, database) {
            val isPasswordUpdated = UserPasswords.update({ UserPasswords.userId eq userId }) {
                it[passwordHash] = password
            } > 0
            if (!isPasswordUpdated) UserPasswords.insert {
                it[UserPasswords.userId] = userId
                it[passwordHash] = password
            }
        }
    }

    private fun <T> Users.setUser(
        updateBuilder: UpdateBuilder<T>,
        user: UserDto
    ) {
        updateBuilder[username] = user.username
        updateBuilder[email] = user.email
        updateBuilder[displayName] = user.displayName
        updateBuilder[phone] = user.phone
        updateBuilder[fullName] = user.fullName
        updateBuilder[street] = user.address?.street
        updateBuilder[city] = user.address?.city
        updateBuilder[state] = user.address?.state
        updateBuilder[zip] = user.address?.zip
        updateBuilder[photoUrl] = user.photoUrl
    }

    private fun ResultRow.toUserDto() = UserDto(
        id = this[Users.id].value,
        email = this[Users.email],
        username = this[Users.username],
        displayName = this[Users.displayName],
        phone = this[Users.phone],
        fullName = this[Users.fullName],
        photoUrl = this[Users.photoUrl],
        address = this.toAddressDto()
    )

    private fun ResultRow.toAddressDto() = this[Users.street]?.let {
        AddressDto(
            street = it,
            city = this[Users.city]!!,
            state = this[Users.state]!!,
            zip = this[Users.zip]!!
        )
    }
}