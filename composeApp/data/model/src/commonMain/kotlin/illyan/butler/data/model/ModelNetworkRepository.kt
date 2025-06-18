package illyan.butler.data.model

import com.aallam.openai.client.OpenAI
import illyan.butler.core.local.datasource.CredentialLocalDataSource
import illyan.butler.core.network.datasource.ModelNetworkDataSource
import illyan.butler.core.network.ktor.http.di.provideOpenAIClient
import illyan.butler.data.settings.AppRepository
import illyan.butler.shared.model.auth.ApiKeyCredential
import illyan.butler.shared.model.chat.AiSource
import illyan.butler.shared.model.chat.ApiType
import io.github.aakira.napier.Napier
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.update
import org.koin.core.annotation.Single

@OptIn(ExperimentalCoroutinesApi::class)
@Single
class ModelNetworkRepository(
    private val modelNetworkDataSource: ModelNetworkDataSource,
    private val credentialLocalDataSource: CredentialLocalDataSource,
    private val appRepository: AppRepository
) : ModelRepository {
    private val cachedOpenAIClients = hashMapOf<ApiKeyCredential, OpenAI>()
    private val healthyCredentials = MutableStateFlow(emptyList<ApiKeyCredential>())
    override val healthyHostCredentials: Flow<List<ApiKeyCredential>> = healthyCredentials
    override fun getAiSources(): Flow<List<AiSource>> = combine(
        appRepository.signedInServers.flatMapLatest { sources ->
            sources.map { source ->
                flowOf(
                    modelNetworkDataSource.fetchAll(source).map {
                        AiSource.Server(
                            source = AiSource.Api(
                                modelId = it.id,
                                endpoint = it.endpoint,
                                apiType = ApiType.OPENAI
                            )
                        )
                    }
                )
            }.takeIf { it.isNotEmpty() }?.let { sourceFlows ->
                combine(*sourceFlows.toTypedArray()) { it.toList().flatten() }
            } ?: flowOf(emptyList())
        },
        getAvailableModelsFromProviders()
    ) { serverSources, openaiModels ->
        (serverSources + openaiModels).distinctBy { it.modelId }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getAvailableModelsFromProviders(): Flow<List<AiSource>> = credentialLocalDataSource.getCredentials().flatMapLatest { credentials ->
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
                        AiSource.Api(
                            modelId = it.id.id,
                            endpoint = credential.providerUrl,
                            apiType = ApiType.OPENAI
                        )
                    }
                    healthyCredentials.update { list -> list + credential }
                    emit(models)
                } catch (e: Exception) {
                    Napier.e(e) { "Failed to fetch models for ${credential.providerUrl}" }
                    healthyCredentials.update { list -> list - credential }
                    emit(emptyList())
                }
            }.flowOn(Dispatchers.IO)
        }.toTypedArray()) { it.toList().flatten() }
    }
}
