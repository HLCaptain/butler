#if (${PACKAGE_NAME} && ${PACKAGE_NAME} != "")package ${PACKAGE_NAME}
    #set($firstChar = $NAME.substring(0, 1).toLowerCase())
    #set($restOfString = $NAME.substring(1))
    #set($camelName = "${firstChar}${restOfString}")

#end
#parse("File Header.java")
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.filterNot
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import nest.planty.data.mapping.toDomainModel
import nest.planty.util.log.randomUUID
import org.koin.core.annotation.Single

@Single
class ${NAME}Manager(
    private val authManager: AuthManager,
    private val ${camelName}Repository: ${NAME}Repository
) {
    // Custom get logic with flows
    @OptIn(ExperimentalCoroutinesApi::class)
    val owned${NAME} = authManager.signedInUser.flatMapLatest { user ->
        user?.uid?.let {
            ${camelName}Repository.get${NAME}ByOwner(it)
        }?.filterNot { it.second }?.map { it.first } ?: flowOf(emptyList())
    }

    suspend fun own${NAME}(uuid: String) {
        authManager.signedInUser.map { it?.uid }.first()?.let { userUUID ->
            ${camelName}Repository.get${NAME}(uuid).first { !it.second }.let { (${camelName}ToOwn, _) ->
                ${camelName}ToOwn?.copy(ownerUUID = userUUID)?.let { ${camelName}Repository.upsert(it) }
                Napier.d("User owns ${NAME}")
            }
        }
    }
    
    suspend fun delete${NAME}(${camelName}UUID: String) {
        ${camelName}Repository.delete(${camelName}UUID)
    }
    
    suspend fun delete${NAME}ByOwner() {
        authManager.signedInUser.map { it?.uid }.first()?.let { userUUID ->
            ${camelName}Repository.deleteByOwner(userUUID)
        }
    }
}