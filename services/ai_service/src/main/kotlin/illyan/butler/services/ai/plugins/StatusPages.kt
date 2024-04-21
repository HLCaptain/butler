package illyan.butler.services.ai.plugins

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.plugins.statuspages.StatusPagesConfig
import io.ktor.server.response.respond

fun Application.configureStatusPages() {
    install(StatusPages) {
        handleStatusPagesExceptions()
        handleUnauthorizedAccess()
    }
}

private fun StatusPagesConfig.handleStatusPagesExceptions() {
    exception<Exception> { call, t ->
        call.respond(HttpStatusCode.InternalServerError, t.message ?: "Unknown error")
    }
    exception<SecurityException>{ call, throwable ->
        call.respond(HttpStatusCode.Unauthorized)
    }
}

private fun StatusPagesConfig.handleUnauthorizedAccess() {
    status(HttpStatusCode.Unauthorized) { call, _ ->
        call.respond(HttpStatusCode.Unauthorized)
    }
}