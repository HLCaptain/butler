package illyan.butler.server.plugins

import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.sse.SSE

fun Application.configureSSE() {
    install(SSE)
}
