#if (${PACKAGE_NAME} && ${PACKAGE_NAME} != "")package ${PACKAGE_NAME}

#end
#parse("File Header.java")
import kotlinx.coroutines.flow.Flow

interface ${NAME}NetworkDataSource {
    fun fetch(uuid: String): Flow<Network${NAME}>
    fun fetchByOwner(ownerUUID: String): Flow<List<Network${NAME}>>
    suspend fun upsert(data: Network${NAME})
    suspend fun delete(uuid: String)
    suspend fun deleteByOwner(ownerUUID: String)
}