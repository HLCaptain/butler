package illyan.butler.core.network.ktor.http

import illyan.butler.core.network.datasource.HostNetworkDataSource
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.http.isSuccess
import org.koin.core.annotation.Single

@Single
class HostHttpDataSource(
    private val unauthorizedClientFactory: (String) -> HttpClient,
) : HostNetworkDataSource {
    override suspend fun tryToConnect(url: String): Boolean {
        return unauthorizedClientFactory(url).get(url).status.isSuccess() // Should get Hello World JSON message
    }
}
