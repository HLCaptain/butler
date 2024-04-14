package illyan.butler.api_gateway.plugins

import illyan.butler.api_gateway.endpoints.utils.WebsocketContentConverterWithFallback
import illyan.butler.api_gateway.utils.AppConfig
import io.ktor.serialization.kotlinx.KotlinxWebsocketSerializationConverter
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.websocket.WebSockets

fun Application.configureWebSockets() {
    install(WebSockets) {
//        pingPeriod = 10000.seconds.toJavaDuration()
//        timeout = 10000.seconds.toJavaDuration()
//        maxFrameSize = Long.MAX_VALUE
//        masking = false
        contentConverter = WebsocketContentConverterWithFallback(
            AppConfig.Ktor.SERIALIZATION_FORMATS.map { KotlinxWebsocketSerializationConverter(it) }
        )
    }
}
