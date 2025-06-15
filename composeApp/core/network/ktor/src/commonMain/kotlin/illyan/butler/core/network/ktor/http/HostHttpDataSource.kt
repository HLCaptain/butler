package illyan.butler.core.network.ktor.http

import illyan.butler.core.network.datasource.HostNetworkDataSource
import illyan.butler.core.network.ktor.http.di.KtorUnauthorizedHttpClientFactory
import io.ktor.client.request.get
import io.ktor.http.isSuccess
import kotlinx.serialization.ExperimentalSerializationApi
import org.koin.core.annotation.Single

@OptIn(ExperimentalSerializationApi::class)
@Single
class HostHttpDataSource(
    private val unauthorizedClientFactory: KtorUnauthorizedHttpClientFactory
) : HostNetworkDataSource {
    override suspend fun tryToConnect(url: String): Boolean {
        return unauthorizedClientFactory(url).get(url).status.isSuccess() // Should get Hello World JSON message
    }
}
