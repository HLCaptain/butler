package illyan.butler.data.ktor.datasource

import illyan.butler.data.network.datasource.HostNetworkDataSource
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.http.isSuccess
import org.koin.core.annotation.Single

@Single
class HostKtorDataSource(
    private val client: HttpClient
) : HostNetworkDataSource {
    override suspend fun tryToConnect(url: String): Boolean {
        return client.get(url).status.isSuccess() // Should get Hello World JSON message
    }
}