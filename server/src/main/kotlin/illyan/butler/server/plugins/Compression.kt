package illyan.butler.server.plugins

import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.compression.Compression
import io.ktor.server.plugins.compression.condition
import io.ktor.server.plugins.compression.deflate
import io.ktor.server.plugins.compression.gzip

fun Application.configureCompression() {
    install(Compression) {
        gzip {
            priority = 1.0
            condition { call ->
                (call.contentLength ?: 0) > 256
            }
        }

        deflate {
            priority = 10.0
            condition { call ->
                (call.contentLength ?: 0) > 256
            }
        }
    }
}
