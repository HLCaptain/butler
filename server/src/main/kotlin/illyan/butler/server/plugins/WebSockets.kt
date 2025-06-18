package illyan.butler.server.plugins

import illyan.butler.server.AppConfig
import io.ktor.serialization.kotlinx.KotlinxWebsocketSerializationConverter
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.websocket.WebSockets
import io.ktor.websocket.WebSocketDeflateExtension
import okio.Deflater

fun Application.configureWebSockets() {
    install(WebSockets) {
        contentConverter = KotlinxWebsocketSerializationConverter(AppConfig.Ktor.DEFAULT_SERIALIZATION_FORMAT)

        extensions {
            install(WebSocketDeflateExtension) {
                compressionLevel = Deflater.DEFAULT_COMPRESSION
                compressIfBiggerThan(4096) // 4KB
            }
        }
    }
}
