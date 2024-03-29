#if (${PACKAGE_NAME} && ${PACKAGE_NAME} != "")package ${PACKAGE_NAME}
    #set($firstChar = $NAME.substring(0, 1).toLowerCase())
    #set($restOfString = $NAME.substring(1))
    #set($camelName = "${firstChar}${restOfString}")

#end
#parse("File Header.java")
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.dropWhile
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import org.koin.core.annotation.Single
import org.mobilenativefoundation.store.store5.ExperimentalStoreApi
import org.mobilenativefoundation.store.store5.StoreReadRequest
import org.mobilenativefoundation.store.store5.StoreReadResponse
import org.mobilenativefoundation.store.store5.StoreWriteRequest

@OptIn(ExperimentalStoreApi::class)
@Single
class ${NAME}Repository(
    ${camelName}MutableStoreBuilder: ${NAME}MutableStoreBuilder,
    owned${NAME}MutableStoreBuilder: Owned${NAME}MutableStoreBuilder,
    private val coroutineScopeIO: CoroutineScope
) {
    @OptIn(ExperimentalStoreApi::class)
    private val ${camelName}MutableStore = ${camelName}MutableStoreBuilder.store
    @OptIn(ExperimentalStoreApi::class)
    private val owned${NAME}MutableStore = owned${NAME}MutableStoreBuilder.store

    private val ${camelName}StateFlows = mutableMapOf<String, StateFlow<Pair<Domain${NAME}?, Boolean>>>()
    @OptIn(ExperimentalStoreApi::class)
    fun get${NAME}(uuid: String): StateFlow<Pair<Domain${NAME}?, Boolean>> {
        return ${camelName}StateFlows.getOrPut(uuid) {
            ${camelName}MutableStore.stream<StoreReadResponse<Domain${NAME}>>(
                StoreReadRequest.fresh(key = uuid)
            ).map {
                it.throwIfError()
                val data = it.dataOrNull()
                data to (it is StoreReadResponse.Loading)
            }.stateIn(
                coroutineScopeIO,
                SharingStarted.Eagerly,
                null to true
            )
        }
    }
    
    private val owned${NAME}StateFlows = mutableMapOf<String, StateFlow<Pair<List<Domain${NAME}>?, Boolean>>>()
    fun get${NAME}ByOwner(ownerUUID: String): StateFlow<Pair<List<Domain${NAME}>?, Boolean>> {
        return owned${NAME}StateFlows.getOrPut(ownerUUID) {
            owned${NAME}MutableStore.stream<StoreReadResponse<List<Domain${NAME}>>>(
                StoreReadRequest.fresh(key = ownerUUID)
            ).dropWhile {
                it is StoreReadResponse.Loading
            }.map {
                it.throwIfError()
                val data = it.dataOrNull()
                data to (it is StoreReadResponse.Loading)
            }.stateIn(
                coroutineScopeIO,
                SharingStarted.Eagerly,
                null to true
            )
        }
    }

    @OptIn(ExperimentalStoreApi::class)
    suspend fun upsert(${camelName}: Domain${NAME}) {
        ${camelName}MutableStore.write(
            StoreWriteRequest.of(
                key = ${camelName}.uuid,
                value = ${camelName}
            )
        )
    }
    
    suspend fun delete(uuid: String) {
        ${camelName}MutableStore.clear(uuid)
    }
    
    suspend fun deleteByOwner(ownerUUID: String) {
        owned${NAME}MutableStore.clear(ownerUUID)
    }
}