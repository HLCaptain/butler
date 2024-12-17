package illyan.butler.data.resource

import illyan.butler.core.local.datasource.DataHistoryLocalDataSource
import illyan.butler.core.local.datasource.ResourceLocalDataSource
import illyan.butler.core.network.datasource.ResourceNetworkDataSource
import illyan.butler.core.sync.NoopConverter
import illyan.butler.core.sync.provideBookkeeper
import illyan.butler.domain.model.DomainResource
import org.koin.core.annotation.Single
import org.mobilenativefoundation.store.core5.ExperimentalStoreApi
import org.mobilenativefoundation.store.store5.Fetcher
import org.mobilenativefoundation.store.store5.MutableStoreBuilder
import org.mobilenativefoundation.store.store5.SourceOfTruth
import org.mobilenativefoundation.store.store5.Updater
import org.mobilenativefoundation.store.store5.UpdaterResult
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Single
class ResourceMutableStoreBuilder(
    resourceLocalDataSource: ResourceLocalDataSource,
    resourceNetworkDataSource: ResourceNetworkDataSource,
    dataHistoryLocalDataSource: DataHistoryLocalDataSource
) {
    @OptIn(ExperimentalStoreApi::class)
    val store = provideResourceMutableStore(resourceLocalDataSource, resourceNetworkDataSource, dataHistoryLocalDataSource)
}

@OptIn(ExperimentalStoreApi::class, ExperimentalUuidApi::class)
fun provideResourceMutableStore(
    resourceLocalDataSource: ResourceLocalDataSource,
    resourceNetworkDataSource: ResourceNetworkDataSource,
    dataHistoryLocalDataSource: DataHistoryLocalDataSource
) = MutableStoreBuilder.from(
    fetcher = Fetcher.ofFlow { key ->
        require(key is ResourceKey.Read.ByResourceId)
        resourceNetworkDataSource.fetchResourceById(key.resourceId)
    },
    sourceOfTruth = SourceOfTruth.of(
        reader = { key ->
            require(key is ResourceKey.Read.ByResourceId)
            resourceLocalDataSource.getResource(key.resourceId)
        },
        writer = { key, local ->
            when (key) {
                is ResourceKey.Write.Create -> resourceLocalDataSource.upsertResource(local.copy(id = Uuid.random().toString()))
                is ResourceKey.Write.Upsert -> resourceLocalDataSource.upsertResource(local)
                is ResourceKey.Read.ByResourceId -> resourceLocalDataSource.upsertResource(local) // From fetcher
                else -> throw IllegalArgumentException("Unsupported key type: ${key::class.qualifiedName}")
            }
        },
        delete = { key ->
            require(key is ResourceKey.Delete)
            when (key) {
                is ResourceKey.Delete.ByResourceId -> resourceLocalDataSource.deleteResourceById(key.resourceId)
                is ResourceKey.Delete.All -> resourceLocalDataSource.deleteAllResources()
            }
        },
        deleteAll = {
            resourceLocalDataSource.deleteAllResources()
        }
    ),
    converter = NoopConverter(),
).build(
    updater = Updater.by(
        post = { key, output ->
            require(key is ResourceKey.Write)
            val newResource = when (key) {
                is ResourceKey.Write.Create -> resourceNetworkDataSource.upsert(output).also {
                    resourceLocalDataSource.replaceResource(it.id!!, it)
                }
                is ResourceKey.Write.Upsert -> resourceNetworkDataSource.upsert(output)
            }
            UpdaterResult.Success.Typed(newResource)
        },
        onCompletion = null
    ),
    bookkeeper = provideBookkeeper(
        dataHistoryLocalDataSource,
        DomainResource::class.qualifiedName.toString()
    ) { it.toString() }
)