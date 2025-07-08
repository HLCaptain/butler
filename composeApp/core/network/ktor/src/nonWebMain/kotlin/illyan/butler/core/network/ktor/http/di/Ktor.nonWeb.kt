package illyan.butler.core.network.ktor.http.di

import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.HttpClientEngineConfig
import io.ktor.client.engine.cio.CIOEngineConfig
import io.ktor.network.tls.CIOCipherSuites

actual fun<T : HttpClientEngineConfig> HttpClientConfig<T>.setupPlatformHttpClient() {
    engine {
        if (this is CIOEngineConfig) {
            https {
                serverName = null
                cipherSuites = CIOCipherSuites.SupportedSuites
            }
        }
    }
}
