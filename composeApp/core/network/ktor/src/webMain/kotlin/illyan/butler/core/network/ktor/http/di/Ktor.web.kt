package illyan.butler.core.network.ktor.http.di

import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.HttpClientEngineConfig

actual fun <T : HttpClientEngineConfig> HttpClientConfig<T>.setupPlatformHttpClient() {

}