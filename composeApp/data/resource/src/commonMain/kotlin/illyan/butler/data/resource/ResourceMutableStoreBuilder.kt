package illyan.butler.data.resource

import illyan.butler.core.local.datasource.DataHistoryLocalDataSource
import illyan.butler.core.local.datasource.ResourceLocalDataSource
import illyan.butler.core.network.datasource.ResourceNetworkDataSource
import illyan.butler.core.sync.NoopConverter
import illyan.butler.core.sync.provideBookkeeper
import illyan.butler.domain.model.Resource
import illyan.butler.shared.model.chat.Source
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
class ResourceMutableStoreBuilder(
    resourceLocalDataSource: ResourceLocalDataSource,
    resourceNetworkDataSource: ResourceNetworkDataSource,
    dataHistoryLocalDataSource: DataHistoryLocalDataSource
) {
    @OptIn(ExperimentalStoreApi::class)
    val store = provideResourceMutableStore(resourceLocalDataSource, resourceNetworkDataSource, dataHistoryLocalDataSource)
}

@OptIn(ExperimentalStoreApi::class, ExperimentalUuidApi::class, ExperimentalTime::class)
fun provideResourceMutableStore(
    resourceLocalDataSource: ResourceLocalDataSource,
    resourceNetworkDataSource: ResourceNetworkDataSource,
    dataHistoryLocalDataSource: DataHistoryLocalDataSource
) = MutableStoreBuilder.from(
    fetcher = Fetcher.ofFlow<ResourceKey, Resource> { key ->
        require(key is ResourceKey.Read.ByResourceId)
        require(key.source is Source.Server)
        resourceNetworkDataSource.fetchResourceById(key.source, key.resourceId)
    },
    sourceOfTruth = SourceOfTruth.of(
        reader = { key ->
            require(key is ResourceKey.Read.ByResourceId)
            resourceLocalDataSource.getResource(key.resourceId)
        },
        writer = { key, local ->
            when (key) {
                ResourceKey.Write.Upsert -> resourceLocalDataSource.upsertResource(local)
                is ResourceKey.Read.ByResourceId -> resourceLocalDataSource.upsertResource(local) // From fetcher
                else -> throw IllegalArgumentException("Unsupported key mimeType: ${key::class.qualifiedName}")
            }
        },
        delete = { key ->
            require(key is ResourceKey.Delete)
            if (!key.resource.deviceOnly) {
                resourceNetworkDataSource.delete(key.resource)
            }
            resourceLocalDataSource.deleteResourceById(key.resource.id)
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
                is ResourceKey.Write.Create -> if (output.deviceOnly) {
                    output // Don't sync to network
                } else {
                    resourceNetworkDataSource.upsert(output).also {
                        resourceLocalDataSource.replaceResource(output.id, it)
                    }
                }
                is ResourceKey.Write.Upsert -> if (output.deviceOnly) {
                    output // Don't sync to network
                } else {
                    resourceNetworkDataSource.upsert(output)
                }
            }
            UpdaterResult.Success.Typed(newResource)
        },
        onCompletion = null
    ),
    bookkeeper = provideBookkeeper(
        dataHistoryLocalDataSource,
        Resource::class.qualifiedName.toString()
    ) { it.toString() }
)