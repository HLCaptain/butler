#if (${PACKAGE_NAME} && ${PACKAGE_NAME} != "")package ${PACKAGE_NAME}
    #set($firstChar = $NAME.substring(0, 1).toLowerCase())
    #set($restOfString = $NAME.substring(1))
    #set($camelName = "${firstChar}${restOfString}")

#end
#parse("File Header.java")
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Single
import org.mobilenativefoundation.store.store5.Converter
import org.mobilenativefoundation.store.store5.ExperimentalStoreApi
import org.mobilenativefoundation.store.store5.Fetcher
import org.mobilenativefoundation.store.store5.MutableStoreBuilder
import org.mobilenativefoundation.store.store5.OnUpdaterCompletion
import org.mobilenativefoundation.store.store5.SourceOfTruth
import org.mobilenativefoundation.store.store5.Updater
import org.mobilenativefoundation.store.store5.UpdaterResult

@Single
class Owned${NAME}MutableStoreBuilder(
    databaseHelper: DatabaseHelper,
    networkDataSource: ${NAME}NetworkDataSource
) {
    @OptIn(ExperimentalStoreApi::class)
    val store = provideOwned${NAME}MutableStore(databaseHelper, networkDataSource)
}

@OptIn(ExperimentalStoreApi::class)
fun provideOwned${NAME}MutableStore(
    databaseHelper: DatabaseHelper,
    networkDataSource: ${NAME}NetworkDataSource
) = MutableStoreBuilder.from(
    fetcher = Fetcher.ofFlow { key ->
        Napier.d("Fetching ${${NAME}::class.simpleName}s")
        networkDataSource.fetchByOwner(key)
    },
    sourceOfTruth = SourceOfTruth.of(
        reader = { key: String ->
            databaseHelper.queryAsListFlow {
                Napier.d("Reading ${${NAME}::class.simpleName}s")
                it.${camelName}Queries.selectByOwner(key)
            }.map { ${camelName}s ->
                ${camelName}s.map { it.toDomainModel() }
            }
        },
        writer = { key, local ->
            databaseHelper.withDatabase { db ->
                local.forEach {
                    Napier.d("Writing ${${NAME}::class.simpleName}s")
                    db.${camelName}Queries.upsert(it)
                }
            }
            local.forEach { networkDataSource.upsert(it.toNetworkModel()) }
        },
        delete = { key ->
            databaseHelper.withDatabase {
                Napier.d("Deleting ${${NAME}::class.simpleName}s")
                it.${camelName}Queries.deleteByOwner(key)
            }
            networkDataSource.deleteByOwner(key)
        },
        deleteAll = {
            databaseHelper.withDatabase {
                Napier.d("Deleting all ${${NAME}::class.simpleName}s")
                it.${camelName}Queries.deleteAll()
            }
        }
    ),
    converter = Converter.Builder<List<Network${NAME}>, List<${NAME}>, List<Domain${NAME}>>()
        .fromOutputToLocal { ${camelName}s -> ${camelName}s.map { it.toLocalModel() } }
        .fromNetworkToLocal { ${camelName}s -> ${camelName}s.map { it.toLocalModel() } }
        .build(),
).build(
    updater = Updater.by(
        post = { key, output ->
            output.forEach {
                networkDataSource.upsert(it.toNetworkModel())
            }
            UpdaterResult.Success.Typed(output)
        },
        onCompletion = OnUpdaterCompletion(
            onSuccess = { _ ->
                Napier.d("Successfully updated ${${NAME}::class.simpleName}s")
            },
            onFailure = { _ ->
                Napier.d("Failed to update ${${NAME}::class.simpleName}s")
            }
        )
    ),
    bookkeeper = provideBookkeeper(
        databaseHelper,
        ${NAME}::class.simpleName.toString() + "List"
    ) { it }
)