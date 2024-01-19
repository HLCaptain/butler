package ai.nest.api_gateway.plugins

import ai.nest.api_gateway.di.serializationFormat
import io.ktor.serialization.kotlinx.KotlinxWebsocketSerializationConverter
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.websocket.WebSockets
import io.ktor.server.websocket.pingPeriod
import io.ktor.server.websocket.timeout
import io.ktor.util.Attributes
import kotlinx.serialization.ExperimentalSerializationApi
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration

@OptIn(ExperimentalSerializationApi::class)
fun Application.configureWebSockets(attributes: Attributes) {
    install(WebSockets) {
        contentConverter = KotlinxWebsocketSerializationConverter(attributes.serializationFormat)
        pingPeriod = 10000.seconds.toJavaDuration()
        timeout = 10000.seconds.toJavaDuration()
        maxFrameSize = Long.MAX_VALUE
        masking = false
    }
}
