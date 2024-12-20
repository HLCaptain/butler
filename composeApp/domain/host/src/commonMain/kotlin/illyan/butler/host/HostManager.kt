package illyan.butler.host

import illyan.butler.data.host.HostRepository
import org.koin.core.annotation.Single

@Single
class HostManager(
    private val hostRepository: HostRepository
) {
    val isConnectingToHost = hostRepository.isConnectingToHost
    val currentHost = hostRepository.currentHost

    suspend fun testAndSelectHost(url: String) = hostRepository.testAndSelectHost(url)

    suspend fun testHost(url: String) = hostRepository.testHost(url)
}
