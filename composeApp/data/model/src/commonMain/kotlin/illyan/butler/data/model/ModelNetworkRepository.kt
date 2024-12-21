package illyan.butler.data.model

import com.aallam.openai.client.OpenAI
import com.aallam.openai.client.OpenAIHost
import illyan.butler.core.local.datasource.CredentialLocalDataSource
import illyan.butler.core.network.datasource.ModelNetworkDataSource
import illyan.butler.domain.model.ApiKeyCredential
import illyan.butler.domain.model.DomainModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import org.koin.core.annotation.Single

@Single
class ModelNetworkRepository(
    private val modelNetworkDataSource: ModelNetworkDataSource,
    private val credentialLocalDataSource: CredentialLocalDataSource
) : ModelRepository {
    private val cachedOpenAIClients = hashMapOf<ApiKeyCredential, OpenAI>()
    override fun getAvailableModelsFromServer(): Flow<List<DomainModel>> = flow { emit(modelNetworkDataSource.fetchAll()) }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun getAvailableModelsFromProviders(): Flow<List<DomainModel>> = credentialLocalDataSource.getCredentials().flatMapLatest { credentials ->
        combine(*credentials.map { credential ->
            cachedOpenAIClients.filter { it.key.providerUrl == credential.providerUrl && it.key.apiKey != credential.apiKey }.forEach {
                // Remove clients with old credentials
                cachedOpenAIClients.remove(it.key)
            }
            flow {
                try {
                    emit(emptyList())
                    val models = cachedOpenAIClients.getOrPut(credential) {
                        OpenAI(
                            token = credential.apiKey,
                            host = OpenAIHost(baseUrl = credential.providerUrl)
                        )
                    }.models().map {
                        DomainModel(
                            name = null,
                            id = it.id.id,
                            ownedBy = it.ownedBy,
                            endpoint = credential.providerUrl
                        )
                    }
                    emit(models)
                } catch (e: Exception) {
                    emit(emptyList())
                }
            }
        }.toTypedArray()) { it.toList().flatten() }
    }
}
