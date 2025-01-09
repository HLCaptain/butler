package illyan.butler.data.model

import com.aallam.openai.client.OpenAI
import illyan.butler.core.local.datasource.CredentialLocalDataSource
import illyan.butler.core.network.datasource.ModelNetworkDataSource
import illyan.butler.core.network.ktor.http.di.provideOpenAIClient
import illyan.butler.data.host.HostRepository
import illyan.butler.domain.model.ApiKeyCredential
import illyan.butler.domain.model.DomainModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import org.koin.core.annotation.Single

@Single
class ModelNetworkRepository(
    private val modelNetworkDataSource: ModelNetworkDataSource,
    private val credentialLocalDataSource: CredentialLocalDataSource,
    private val hostRepository: HostRepository
) : ModelRepository {
    private val cachedOpenAIClients = hashMapOf<ApiKeyCredential, OpenAI>()
    private val healthyCredentials = MutableStateFlow(emptyList<ApiKeyCredential>())
    override val healthyHostCredentials: Flow<List<ApiKeyCredential>> = healthyCredentials
    override fun getAvailableModelsFromServer(): Flow<List<DomainModel>> = hostRepository.currentHost.map { modelNetworkDataSource.fetchAll() }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun getAvailableModelsFromProviders(): Flow<List<DomainModel>> = credentialLocalDataSource.getCredentials().flatMapLatest { credentials ->
        combine(*credentials.map { credential ->
            cachedOpenAIClients.filter { it.key.providerUrl == credential.providerUrl && it.key.apiKey != credential.apiKey }.forEach {
                // Remove clients with old credentials
                cachedOpenAIClients.remove(it.key)
                healthyCredentials.update { list -> list - it.key }
            }
            flow {
                try {
                    val models = cachedOpenAIClients.getOrPut(credential) {
                        provideOpenAIClient(credential)
                    }.models().map {
                        DomainModel(
                            name = null,
                            id = it.id.id,
                            ownedBy = it.ownedBy,
                            endpoint = credential.providerUrl
                        )
                    }
                    healthyCredentials.update { list -> list + credential }
                    emit(models)
                } catch (e: Exception) {
                    healthyCredentials.update { list -> list - credential }
                    emit(emptyList())
                }
            }.flowOn(Dispatchers.IO)
        }.toTypedArray()) { it.toList().flatten() }
    }
}
