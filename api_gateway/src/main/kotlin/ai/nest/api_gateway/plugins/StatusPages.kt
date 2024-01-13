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
    exception<SecurityException>{ call, t ->
        respondWithError(call, HttpStatusCode.Unauthorized, t.message?.let { mapOf(401 to it) })
    }
}


private fun StatusPagesConfig.handleUnauthorizedAccess(){
    status(HttpStatusCode.Unauthorized) { call, _ ->
        respondWithError(call, statusCode = HttpStatusCode.Unauthorized , mapOf(401 to "Access denied"))
    }
}