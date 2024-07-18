package illyan.butler.data.store.builder

import illyan.butler.data.local.datasource.DataHistoryLocalDataSource
import illyan.butler.data.local.datasource.ResourceLocalDataSource
import illyan.butler.data.mapping.toDomainModel
import illyan.butler.data.mapping.toNetworkModel
import illyan.butler.data.network.datasource.ResourceNetworkDataSource
import illyan.butler.data.store.key.ResourceKey
import illyan.butler.data.store.provideBookkeeper
import illyan.butler.domain.model.DomainResource
import illyan.butler.utils.randomUUID
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Single
import org.mobilenativefoundation.store.core5.ExperimentalStoreApi
import org.mobilenativefoundation.store.store5.Fetcher
import org.mobilenativefoundation.store.store5.MutableStoreBuilder
import org.mobilenativefoundation.store.store5.SourceOfTruth
import org.mobilenativefoundation.store.store5.Updater
import org.mobilenativefoundation.store.store5.UpdaterResult

@Single
class ResourceMutableStoreBuilder(
    resourceLocalDataSource: ResourceLocalDataSource,
    resourceNetworkDataSource: ResourceNetworkDataSource,
    dataHistoryLocalDataSource: DataHistoryLocalDataSource
) {
    @OptIn(ExperimentalStoreApi::class)
    val store = provideResourceMutableStore(resourceLocalDataSource, resourceNetworkDataSource, dataHistoryLocalDataSource)
}

@OptIn(ExperimentalStoreApi::class)
fun provideResourceMutableStore(
    resourceLocalDataSource: ResourceLocalDataSource,
    resourceNetworkDataSource: ResourceNetworkDataSource,
    dataHistoryLocalDataSource: DataHistoryLocalDataSource
) = MutableStoreBuilder.from(
    fetcher = Fetcher.ofFlow { key ->
        require(key is ResourceKey.Read.ByResourceId)
        resourceNetworkDataSource.fetchResourceById(key.resourceId).map { it.toDomainModel() }
    },
    sourceOfTruth = SourceOfTruth.of(
        reader = { key ->
            require(key is ResourceKey.Read.ByResourceId)
            resourceLocalDataSource.getResource(key.resourceId)
        },
        writer = { key, local ->
            when (key) {
                is ResourceKey.Write.Create -> resourceLocalDataSource.upsertResource(local.copy(id = randomUUID()))
                is ResourceKey.Write.Upsert -> resourceLocalDataSource.upsertResource(local)
                is ResourceKey.Read.ByResourceId -> resourceLocalDataSource.upsertResource(local) // From fetcher
                else -> throw IllegalArgumentException("Unsupported key type: ${key::class.simpleName}")
            }
        },
        delete = { key ->
            require(key is ResourceKey.Delete.ByResourceId)
            resourceLocalDataSource.deleteResourceById(key.resourceId)
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
            val resource = output.toNetworkModel()
            val newResource = when (key) {
                is ResourceKey.Write.Create -> resourceNetworkDataSource.upsert(resource).also {
                    resourceLocalDataSource.replaceResource(it.id!!, it.toDomainModel())
                }
                is ResourceKey.Write.Upsert -> resourceNetworkDataSource.upsert(resource)
            }
            UpdaterResult.Success.Typed(newResource.toDomainModel())
        },
        onCompletion = null
    ),
    bookkeeper = provideBookkeeper(
        dataHistoryLocalDataSource,
        DomainResource::class.simpleName.toString()
    ) { it.toString() }
)