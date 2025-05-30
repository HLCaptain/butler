package illyan.butler.server.data.exposed

import illyan.butler.server.data.db.UserDatabase
import illyan.butler.server.data.schema.UserPasswords
import illyan.butler.server.data.schema.Users
import illyan.butler.server.data.service.ApiException
import illyan.butler.shared.model.identity.AddressDto
import illyan.butler.shared.model.identity.UserDto
import illyan.butler.shared.model.response.StatusCode
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.SqlExpressionBuilder.eq
import org.jetbrains.exposed.v1.core.statements.UpdateBuilder
import org.jetbrains.exposed.v1.r2dbc.R2dbcDatabase
import org.jetbrains.exposed.v1.r2dbc.SchemaUtils
import org.jetbrains.exposed.v1.r2dbc.deleteWhere
import org.jetbrains.exposed.v1.r2dbc.insert
import org.jetbrains.exposed.v1.r2dbc.insertAndGetId
import org.jetbrains.exposed.v1.r2dbc.selectAll
import org.jetbrains.exposed.v1.r2dbc.transactions.suspendTransaction
import org.jetbrains.exposed.v1.r2dbc.update
import org.koin.core.annotation.Single
import org.springframework.security.crypto.password.PasswordEncoder
import java.util.UUID
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlin.uuid.toJavaUuid

@Single
class UserExposedDatabase(
    private val database: R2dbcDatabase,
    private val dispatcher: CoroutineDispatcher,
    private val passwordEncoder: PasswordEncoder,
    coroutineScopeIO: CoroutineScope
) : UserDatabase {
    init {
        coroutineScopeIO.launch {
            suspendTransaction(db = database) {
                SchemaUtils.create(Users, UserPasswords)
            }
        }
    }

    override suspend fun createUser(user: UserDto): UserDto {
        return suspendTransaction(dispatcher, db = database) {
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
        return suspendTransaction(dispatcher, db = database) {
            Users.selectAll().where { Users.id eq Uuid.parse(userId).toJavaUuid() }.firstOrNull()?.toUserDto() ?: throw ApiException(StatusCode.UserNotFound)
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    override suspend fun updateUser(user: UserDto): UserDto {
        return suspendTransaction(dispatcher, db = database) {
            val isUserUpdated = Users.update({ Users.id eq Uuid.parse(user.id!!).toJavaUuid() }) { setUser(it, user) } > 0
            if (isUserUpdated) user else throw ApiException(StatusCode.UserNotFound)
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    override suspend fun deleteUser(userId: String) {
        suspendTransaction(dispatcher, db = database) {
            Users.deleteWhere { id eq Uuid.parse(userId).toJavaUuid() }
        }
    }

    override suspend fun getUserByEmailAndPassword(email: String, password: String): UserDto {
        return suspendTransaction(dispatcher, db = database) {
            val user = Users.selectAll().where {
                Users.email eq email
            }.firstOrNull()?.toUserDto() ?: throw ApiException(StatusCode.UserNotFound)

            val potentialUser = UserPasswords.selectAll().where { UserPasswords.userId eq UUID.fromString(user.id) }.first()
            if (passwordEncoder.matches(password, potentialUser[UserPasswords.passwordHash])) user else throw throw ApiException(StatusCode.UserNotFound)
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    override fun getUserFlow(userId: String): Flow<UserDto> = flow {
        emit(getUser(userId))
    }

    override fun getUserByEmailAndPasswordFlow(email: String, password: String): Flow<UserDto> = flow {
        emit(getUserByEmailAndPassword(email, password))
    }

    override suspend fun upsertPasswordForUser(userId: String, password: String) {
        val encodedPassword = passwordEncoder.encode(password)
        return suspendTransaction(dispatcher, db = database) {
            val userUuid = UUID.fromString(userId)
            val isPasswordUpdated = UserPasswords.update({ UserPasswords.userId eq userUuid }) {
                it[passwordHash] = encodedPassword
            } > 0
            if (!isPasswordUpdated) UserPasswords.insert {
                it[UserPasswords.userId] = userUuid
                it[passwordHash] = encodedPassword
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

