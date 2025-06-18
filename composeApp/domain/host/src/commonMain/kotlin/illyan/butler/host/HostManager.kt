package illyan.butler.host

import illyan.butler.core.network.datasource.HostNetworkDataSource
import illyan.butler.core.network.ktor.http.di.provideOpenAIClient
import illyan.butler.data.credential.CredentialRepository
import illyan.butler.data.host.HostRepository
import illyan.butler.domain.model.DomainModel
import illyan.butler.shared.model.auth.ApiKeyCredential
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withTimeout
import org.koin.core.annotation.Single

@Single
class HostManager(
    private val hostRepository: HostRepository,
    private val hostNetworkDataSource: HostNetworkDataSource,
    private val credentialRepository: CredentialRepository
) {
    private val _isConnectingToHost = MutableStateFlow(false)
    val isConnectingToHost = _isConnectingToHost.asStateFlow()

    val currentHost = hostRepository.currentHost
    val currentCredentials = credentialRepository.apiKeyCredentials

    suspend fun testAndSelectHost(url: String): Boolean {
        return testHost(url).also { isHostAvailable ->
            if (isHostAvailable) hostRepository.upsertHostUrl(url)
        }
    }

    suspend fun testHost(url: String): Boolean {
        if (url.isBlank()) return false
        _isConnectingToHost.update { true }
        return try {
            withTimeout(5000) {
                hostNetworkDataSource.tryToConnect(url)
            }
        } catch (e: Exception) {
            false
        }.also { _isConnectingToHost.update { false } }
    }

    /**
     * @return List of models available on the provider with given credentials
     */
    suspend fun testApiKeyCredential(credential: ApiKeyCredential): List<DomainModel> {
        return try {
            withTimeout(5000) {
                provideOpenAIClient(credential).models().map {
                    DomainModel(
                        name = null,
                        id = it.id.id,
                        ownedBy = it.ownedBy,
                        endpoint = credential.providerUrl
                    )
                }
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun addApiKeyCredential(credential: ApiKeyCredential) {
        credentialRepository.upsertApiKeyCredential(credential)
    }

    suspend fun selectHostWithoutTest(url: String) {
        hostRepository.upsertHostUrl(url)
    }

    suspend fun deleteApiKeyCredential(apiKeyCredential: ApiKeyCredential) {
        credentialRepository.deleteApiKeyCredentialByUrl(apiKeyCredential.providerUrl)
    }
}
