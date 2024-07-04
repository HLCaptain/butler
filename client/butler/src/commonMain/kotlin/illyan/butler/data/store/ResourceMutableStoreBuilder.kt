package illyan.butler.data.store

import illyan.butler.data.local.datasource.DataHistoryLocalDataSource
import illyan.butler.data.local.datasource.ResourceLocalDataSource
import illyan.butler.data.mapping.toDomainModel
import illyan.butler.data.mapping.toNetworkModel
import illyan.butler.data.network.datasource.ResourceNetworkDataSource
import illyan.butler.data.network.model.chat.ResourceDto
import illyan.butler.db.Resource
import illyan.butler.domain.model.DomainResource
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flow
import org.koin.core.annotation.Single
import org.mobilenativefoundation.store.core5.ExperimentalStoreApi
import org.mobilenativefoundation.store.store5.Converter
import org.mobilenativefoundation.store.store5.Fetcher
import org.mobilenativefoundation.store.store5.MutableStoreBuilder
import org.mobilenativefoundation.store.store5.OnUpdaterCompletion
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
        Napier.d("Fetching resource $key")
        combine(
            flow { emit(listOf(resourceNetworkDataSource.fetchResource(key))) },
            flow { emit(emptyList<ResourceDto>()); emitAll(resourceNetworkDataSource.fetchNewResources()) }
        ) { resource, newResources ->
            (newResources + resource).distinctBy { it?.id }.firstOrNull { it?.id == key }
        }.filterNotNull()
    },
    sourceOfTruth = SourceOfTruth.of(
        reader = { key: String ->
            Napier.d("Reading resource at $key")
            resourceLocalDataSource.getResource(key)
        },
        writer = { key, local ->
            Napier.d("Writing resource at $key")
            resourceLocalDataSource.upsertResource(local)
        },
        delete = { key ->
            Napier.d("Deleting resource at $key")
            resourceLocalDataSource.deleteResource(key)
            resourceNetworkDataSource.delete(key)
        },
        deleteAll = {
            Napier.d("Deleting all resources")
            resourceLocalDataSource.deleteAllResources()
        }
    ),
    converter = Converter.Builder<ResourceDto, DomainResource, DomainResource>()
        .fromOutputToLocal { it }
        .fromNetworkToLocal { it.toDomainModel() }
        .build(),
).build(
    updater = Updater.by(
        post = { key, output ->
            val response = resourceNetworkDataSource.upsert(output.toNetworkModel()).toDomainModel()
            UpdaterResult.Success.Typed(response)
        },
        onCompletion = OnUpdaterCompletion(
            onSuccess = { success ->
                Napier.d("Successfully updated resource")
            },
            onFailure = { _ ->
                Napier.d("Failed to update resource")
            }
        )
    ),
    bookkeeper = provideBookkeeper(
        dataHistoryLocalDataSource,
        Resource::class.simpleName.toString()
    ) { it }
)