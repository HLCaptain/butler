package illyan.butler.data.user

import illyan.butler.core.local.datasource.UserLocalDataSource
import illyan.butler.domain.model.User
import illyan.butler.shared.model.chat.Source
import io.github.aakira.napier.Napier
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Single
import org.mobilenativefoundation.store.core5.ExperimentalStoreApi
import org.mobilenativefoundation.store.store5.StoreReadRequest
import org.mobilenativefoundation.store.store5.StoreReadResponse
import org.mobilenativefoundation.store.store5.StoreWriteRequest
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class, ExperimentalStoreApi::class, ExperimentalCoroutinesApi::class)
@Single
class UserStoreRepository(
    userMutableStoreBuilder: UserMutableStoreBuilder,
    private val userLocalDataSource: UserLocalDataSource,
) : UserRepository {
    val store = userMutableStoreBuilder.store

    val userStateFlows = mutableMapOf<Uuid, Flow<User?>>()

    override fun getUser(source: Source.Server): Flow<User?> {
        return userStateFlows.getOrPut(source.userId) {
            store.stream<StoreReadResponse<User>>(
                StoreReadRequest.cached(UserKey.Read.BySource(source), true)
            ).map {
                it.throwIfError()
                Napier.d("getUser Read Response: ${it::class.qualifiedName}")
                val data = it.dataOrNull()
                Napier.d("User ID: ${data?.id}")
                data
            }
        }
    }

    override fun getAllUsers(): Flow<List<User>> {
        return userLocalDataSource.getAllUsers().flatMapLatest { users ->
            val userFlows = users.map {
                getUser(Source.Server(it.id, it.endpoint))
            }
            if (userFlows.isEmpty()) {
                flowOf(emptyList())
            } else {
                combine(userFlows) { userList ->
                    userList.filterNotNull()
                }
            }
        }
    }

    override suspend fun upsertUser(user: User) {
        store.write(
            StoreWriteRequest.of(
                key = UserKey.Write,
                value = user
            )
        )
    }

    override suspend fun deleteUser(userId: Uuid) {
        store.clear(UserKey.Delete(userId))
    }
}
