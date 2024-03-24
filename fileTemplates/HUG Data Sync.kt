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
class ${NAME}MutableStoreBuilder(
    databaseHelper: DatabaseHelper,
    networkDataSource: ${NAME}NetworkDataSource
) {
    @OptIn(ExperimentalStoreApi::class)
    val store = provide${NAME}MutableStore(databaseHelper, networkDataSource)
}

@OptIn(ExperimentalStoreApi::class)
fun provide${NAME}MutableStore(
    databaseHelper: DatabaseHelper,
    networkDataSource: ${NAME}NetworkDataSource
) = MutableStoreBuilder.from(
    fetcher = Fetcher.ofFlow { key ->
        Napier.d("Fetching ${${NAME}::class.simpleName}")
        networkDataSource.fetch(uuid = key)
    },
    sourceOfTruth = SourceOfTruth.of(
        reader = { key: String ->
            databaseHelper.queryAsOneOrNullFlow {
                Napier.d("Reading ${${NAME}::class.simpleName}")
                it.${camelName}Queries.select(key)
            }.map { it?.toDomainModel() }
        },
        writer = { key, local ->
            databaseHelper.withDatabase { db ->
                Napier.d("Writing ${${NAME}::class.simpleName}")
                db.${camelName}Queries.upsert(local)
            }
        },
        delete = { key ->
            databaseHelper.withDatabase {
                Napier.d("Deleting ${${NAME}::class.simpleName}")
                it.${camelName}Queries.delete(key)
            }
            networkDataSource.delete(uuid = key)
        },
        deleteAll = {
            databaseHelper.withDatabase {
                Napier.d("Deleting all ${${NAME}::class.simpleName}s")
                it.${camelName}Queries.deleteAll()
            }
        }
    ),
    converter = Converter.Builder<Network${NAME}, ${NAME}, Domain${NAME}>()
        .fromOutputToLocal { it.toLocalModel() }
        .fromNetworkToLocal { it.toLocalModel() }
        .build(),
).build(
    updater = Updater.by(
        post = { key, output ->
            networkDataSource.upsert(output.toNetworkModel())
            UpdaterResult.Success.Typed(output)
        },
        onCompletion = OnUpdaterCompletion(
            onSuccess = { _ ->
                Napier.d("Successfully updated ${${NAME}::class.simpleName}")
            },
            onFailure = { _ ->
                Napier.d("Failed to update ${${NAME}::class.simpleName}")
            }
        )
    ),
    bookkeeper = provideBookkeeper(
        databaseHelper,
        ${NAME}::class.simpleName.toString()
    ) { it }
)