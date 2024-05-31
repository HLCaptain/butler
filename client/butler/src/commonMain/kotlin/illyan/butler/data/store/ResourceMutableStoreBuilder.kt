package illyan.butler.data.store

import illyan.butler.data.mapping.toDomainModel
import illyan.butler.data.mapping.toLocalModel
import illyan.butler.data.mapping.toNetworkModel
import illyan.butler.data.network.datasource.ResourceNetworkDataSource
import illyan.butler.data.network.model.chat.ResourceDto
import illyan.butler.data.sqldelight.DatabaseHelper
import illyan.butler.db.Resource
import illyan.butler.domain.model.DomainResource
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
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
    databaseHelper: DatabaseHelper,
    resourceNetworkDataSource: ResourceNetworkDataSource
) {
    @OptIn(ExperimentalStoreApi::class)
    val store = provideResourceMutableStore(databaseHelper, resourceNetworkDataSource)
}

@OptIn(ExperimentalStoreApi::class)
fun provideResourceMutableStore(
    databaseHelper: DatabaseHelper,
    resourceNetworkDataSource: ResourceNetworkDataSource
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
            databaseHelper.queryAsOneOrNullFlow {
                Napier.d("Reading resource at $key")
                it.resourceQueries.select(key)
            }.map { it?.toDomainModel() }
        },
        writer = { key, local ->
            databaseHelper.withDatabase { db ->
                Napier.d("Writing resource at $key")
                db.resourceQueries.upsert(local)
            }
        },
        delete = { key ->
            var chatId = ""
            databaseHelper.withDatabase {
                Napier.d("Deleting resource at $key")
                it.resourceQueries.delete(key)
            }
            resourceNetworkDataSource.delete(key)
        },
        deleteAll = {
            databaseHelper.withDatabase {
                Napier.d("Deleting all resources")
                it.messageQueries.deleteAll()
            }
        }
    ),
    converter = Converter.Builder<ResourceDto, Resource, DomainResource>()
        .fromOutputToLocal { it.toLocalModel() }
        .fromNetworkToLocal { it.toLocalModel() }
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
        databaseHelper,
        Resource::class.simpleName.toString()
    ) { it }
)