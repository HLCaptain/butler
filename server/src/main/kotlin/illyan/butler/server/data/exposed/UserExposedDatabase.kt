package illyan.butler.server.data.exposed

import illyan.butler.server.data.db.UserDatabase
import illyan.butler.server.data.schema.UserPasswords
import illyan.butler.server.data.schema.Users
import illyan.butler.server.data.service.ApiException
import illyan.butler.shared.model.identity.AddressDto
import illyan.butler.shared.model.identity.UserDto
import illyan.butler.shared.model.response.StatusCode
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
import org.springframework.security.crypto.password.PasswordEncoder
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlin.uuid.toJavaUuid

@Single
class UserExposedDatabase(
    private val database: Database,
    private val dispatcher: CoroutineDispatcher,
    private val passwordEncoder: PasswordEncoder
) : UserDatabase {
    init {
        transaction(database) {
            SchemaUtils.create(Users, UserPasswords)
        }
    }

    override suspend fun createUser(user: UserDto): UserDto {
        return newSuspendedTransaction(dispatcher, database) {
            val userId = try {
                Users.insertAndGetId { setUser(it, user) }
            } catch (e: Exception) {
                throw throw ApiException(StatusCode.UserAlreadyExists)
            }
            user.copy(id = userId.value.toString())
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    override suspend fun getUser(userId: String): UserDto {
        return newSuspendedTransaction(dispatcher, database) {
            Users.selectAll().where { Users.id eq Uuid.parse(userId).toJavaUuid() }.firstOrNull()?.toUserDto() ?: throw ApiException(StatusCode.UserNotFound)
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    override suspend fun updateUser(user: UserDto): UserDto {
        return newSuspendedTransaction(dispatcher, database) {
            val isUserUpdated = Users.update({ Users.id eq Uuid.parse(user.id!!).toJavaUuid() }) { setUser(it, user) } > 0
            if (isUserUpdated) user else throw ApiException(StatusCode.UserNotFound)
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    override suspend fun deleteUser(userId: String) {
        newSuspendedTransaction(dispatcher, database) {
            Users.deleteWhere { id eq Uuid.parse(userId).toJavaUuid() }
        }
    }

    override suspend fun getUserByEmailAndPassword(email: String, password: String): UserDto {
        return newSuspendedTransaction(dispatcher, database) {
            val user = Users.selectAll().where {
                Users.email eq email
            }.firstOrNull()?.toUserDto() ?: throw ApiException(StatusCode.UserNotFound)

            val potentialUser = UserPasswords.selectAll().where { UserPasswords.userId eq user.id!! }.first()
            if (passwordEncoder.matches(password, potentialUser[UserPasswords.hash])) user else throw throw ApiException(StatusCode.UserNotFound)
        }
    }

    override suspend fun upsertPasswordForUser(userId: String, password: String) {
        val encodedPassword = passwordEncoder.encode(password)
        return newSuspendedTransaction(dispatcher, database) {
            val isPasswordUpdated = UserPasswords.update({ UserPasswords.userId eq userId }) {
                it[hash] = encodedPassword
            } > 0
            if (!isPasswordUpdated) UserPasswords.insert {
                it[UserPasswords.userId] = userId
                it[hash] = encodedPassword
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
        id = this[Users.id].value.toString(),
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