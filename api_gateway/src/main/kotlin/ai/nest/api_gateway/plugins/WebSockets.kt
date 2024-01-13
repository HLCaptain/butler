package ai.nest.api_gateway.plugins

import io.ktor.serialization.kotlinx.KotlinxWebsocketSerializationConverter
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.websocket.WebSockets
import io.ktor.server.websocket.pingPeriod
import io.ktor.server.websocket.timeout
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.protobuf.ProtoBuf
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration

@OptIn(ExperimentalSerializationApi::class)
fun Application.configureWebSockets() {
    install(WebSockets) {
        contentConverter = KotlinxWebsocketSerializationConverter(ProtoBuf)
        pingPeriod = 10000.seconds.toJavaDuration()
        timeout = 10000.seconds.toJavaDuration()
        maxFrameSize = Long.MAX_VALUE
        masking = false
    }
}
