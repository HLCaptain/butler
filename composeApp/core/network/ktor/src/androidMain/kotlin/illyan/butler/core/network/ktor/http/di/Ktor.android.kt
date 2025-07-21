package illyan.butler.core.network.ktor.http.di

import illyan.butler.shared.model.chat.Source
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.cio.CIO
import io.ktor.network.tls.CIOCipherSuites

actual fun createPlatformHttpClient(block: HttpClientConfig<*>.() -> Unit): HttpClient {
    return HttpClient(CIO) {
        engine {
            https {
                serverName = null
                cipherSuites = CIOCipherSuites.SupportedSuites
            }
        }
        block()
    }
}
