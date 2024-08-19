package illyan.butler.backend.plugins

import illyan.butler.backend.data.model.response.ServerErrorResponse
import illyan.butler.backend.data.service.ApiException
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
    exception<ApiException> { call, t ->
        call.respond(t.httpStatusCode, ServerErrorResponse(t.statusCodes))
    }
    exception<IllegalArgumentException> { call, _ ->
        call.respond(HttpStatusCode.BadRequest)
    }
    exception<SecurityException> { call, _ ->
        call.respond(HttpStatusCode.Unauthorized)
    }
}

private fun StatusPagesConfig.handleUnauthorizedAccess() {
    status(HttpStatusCode.Unauthorized) { call, _ ->
        call.respond(HttpStatusCode.Unauthorized)
    }
}