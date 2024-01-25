package ai.nest.api_gateway.plugins

import ai.nest.api_gateway.data.utils.LocalizedMessageException
import ai.nest.api_gateway.endpoints.utils.respondWithError
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.plugins.statuspages.StatusPagesConfig

fun Application.configureStatusPages() {
    install(StatusPages) {
        handleStatusPagesExceptions()
        handleUnauthorizedAccess()
    }
}

private fun StatusPagesConfig.handleStatusPagesExceptions() {
    exception<LocalizedMessageException> { call, t ->
        respondWithError(call, HttpStatusCode.BadRequest, t.errorMessages)
    }
    exception<SecurityException>{ call, throwable ->
        // TODO: Log throwable message with OpenTelemetry or Napier
        respondWithError(call, HttpStatusCode.Unauthorized)
    }
}


private fun StatusPagesConfig.handleUnauthorizedAccess() {
    status(HttpStatusCode.Unauthorized) { call, _ ->
        respondWithError(call, HttpStatusCode.Unauthorized)
    }
}