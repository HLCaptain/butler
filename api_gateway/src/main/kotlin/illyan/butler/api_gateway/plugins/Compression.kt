package illyan.butler.api_gateway.plugins

import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.compression.Compression

fun Application.configureCompression() {
    install(Compression)
}