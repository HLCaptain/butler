package illyan.butler.data.user

import illyan.butler.core.local.datasource.DataHistoryLocalDataSource
import illyan.butler.core.local.datasource.UserLocalDataSource
import illyan.butler.core.network.datasource.AuthNetworkDataSource
import illyan.butler.core.sync.NoopConverter
import illyan.butler.core.sync.provideBookkeeper
import illyan.butler.domain.model.User
import org.koin.core.annotation.Single
import org.mobilenativefoundation.store.core5.ExperimentalStoreApi
import org.mobilenativefoundation.store.store5.Fetcher
import org.mobilenativefoundation.store.store5.MutableStoreBuilder
import org.mobilenativefoundation.store.store5.SourceOfTruth
import org.mobilenativefoundation.store.store5.Updater
import org.mobilenativefoundation.store.store5.UpdaterResult
import kotlin.time.ExperimentalTime
import kotlin.uuid.ExperimentalUuidApi

@Single
class UserMutableStoreBuilder(
    userLocalDataSource: UserLocalDataSource,
    userNetworkDataSource: AuthNetworkDataSource,
    dataHistoryLocalDataSource: DataHistoryLocalDataSource
) {
    @OptIn(ExperimentalStoreApi::class)
    val store = provideResourceMutableStore(userLocalDataSource, userNetworkDataSource, dataHistoryLocalDataSource)
}

@OptIn(ExperimentalStoreApi::class, ExperimentalUuidApi::class, ExperimentalTime::class)
fun provideResourceMutableStore(
    userLocalDataSource: UserLocalDataSource,
    userNetworkDataSource: AuthNetworkDataSource,
    dataHistoryLocalDataSource: DataHistoryLocalDataSource
) = MutableStoreBuilder.from(
    fetcher = Fetcher.ofFlow<UserKey, User> { key ->
        require(key is UserKey.Read.BySource)
        userNetworkDataSource.getUser(key.source)
    },
    sourceOfTruth = SourceOfTruth.of(
        reader = { key ->
            require(key is UserKey.Read.BySource)
            userLocalDataSource.getUser(key.source.userId)
        },
        writer = { key, local ->
            when (key) {
                UserKey.Write -> userLocalDataSource.upsertUser(local)
                is UserKey.Read.BySource -> userLocalDataSource.upsertUser(local) // From fetcher
                else -> throw IllegalArgumentException("Unsupported key mimeType: ${key::class.qualifiedName}")
            }
        },
        delete = { key ->
            require(key is UserKey.Delete)
            userLocalDataSource.deleteUser(key.userId)
        },
        deleteAll = {
            userLocalDataSource.deleteUserData()
        }
    ),
    converter = NoopConverter(),
).build(
    updater = Updater.by(
        post = { key, output ->
            require(key is UserKey.Write)
            val user = userNetworkDataSource.updateUserData(output)
            UpdaterResult.Success.Typed(user)
        },
        onCompletion = null
    ),
    bookkeeper = provideBookkeeper(
        dataHistoryLocalDataSource,
        User::class.qualifiedName.toString()
    ) { it.toString() }
)
