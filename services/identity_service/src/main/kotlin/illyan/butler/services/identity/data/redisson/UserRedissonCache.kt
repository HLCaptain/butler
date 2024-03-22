package illyan.butler.services.identity.data.redisson

import illyan.butler.services.identity.data.cache.UserCache
import illyan.butler.services.identity.data.model.identity.UserDto
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import org.koin.core.annotation.Single
import org.redisson.api.RedissonClient

@Single
class UserRedissonCache(
    private val client: RedissonClient,
    private val dispatcher: CoroutineDispatcher
) : UserCache {
    override suspend fun getUser(userId: String): UserDto? {
        return withContext(dispatcher) {
            client.getBucket<UserDto?>("user:$userId").get()
        }
    }

    override fun getUserChanges(userId: String) = callbackFlow {
        val topic = client.getTopic("user:$userId")
        val listenerId = topic.addListenerAsync(UserDto::class.java) { channel, msg ->
            try {
                trySend(msg).isSuccess
            } catch (e: Exception) {
                close(e)
            }
        }.get()

        awaitClose { topic.removeListener(listenerId) }
    }.flowOn(dispatcher)

    override suspend fun setUser(user: UserDto): UserDto {
        return withContext(dispatcher) {
            client.getBucket<UserDto>("user:${user.id}").set(user)
            user
        }
    }

    override suspend fun deleteUser(userId: String) {
        withContext(dispatcher) {
            client.getBucket<UserDto>("user:$userId").delete()
        }
    }
}