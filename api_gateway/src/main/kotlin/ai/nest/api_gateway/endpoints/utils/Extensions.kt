package ai.nest.api_gateway.endpoints.utils

import ai.nest.api_gateway.data.model.localization.LabelDto
import ai.nest.api_gateway.data.model.response.ServerResponse
import ai.nest.api_gateway.plugins.RoleAuthorizationPlugin
import ai.nest.api_gateway.utils.Claim
import ai.nest.api_gateway.utils.Role
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.Parameters
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.RouteSelector
import io.ktor.server.routing.RouteSelectorEvaluation
import io.ktor.server.routing.RoutingResolveContext
import io.ktor.server.websocket.WebSocketServerSession
import io.ktor.util.pipeline.PipelineContext
import java.util.Locale

suspend inline fun <reified T> PipelineContext<Unit, ApplicationCall>.respondWithResult(
    statusCode: HttpStatusCode,
    result: T,
    message: String? = null
) {
    call.respond(statusCode, ServerResponse.success(result, message))
}

suspend fun respondWithError(
    call: ApplicationCall,
    statusCode: HttpStatusCode,
    errorMessage: List<LabelDto>? = null
) {
    call.respond(statusCode, ServerResponse.error(errorMessage, statusCode))
}

fun PipelineContext<Unit, ApplicationCall>.extractLocaleHeader(): Locale {
    val headers = call.request.headers
    return Locale(headers[HttpHeaders.AcceptLanguage]?.trim())
}

fun PipelineContext<Unit, ApplicationCall>.extractApplicationIdHeader(): String {
    val headers = call.request.headers
    return headers[HttpHeaders.UserAgent]?.trim() ?: ""
}

fun WebSocketServerSession.extractLocaleHeaderFromWebSocket(): Locale {
    val headers = call.request.headers
    return Locale(headers[HttpHeaders.AcceptLanguage]?.trim())
}

private fun PipelineContext<Unit, ApplicationCall>.extractPermission(): Int {
    val principal = context.principal<JWTPrincipal>()
    return principal?.getClaim(Claim.PERMISSION, Int::class) ?: -1
}

fun Route.withRoles(vararg roles: Role, build: Route.() -> Unit) {
    // Creating a child route to avoid installing the same plugin twice on a route
    val route = createChild(object : RouteSelector() {
        override fun evaluate(
            context: RoutingResolveContext,
            segmentIndex: Int
        ) = RouteSelectorEvaluation.Transparent
    })
    route.install(RoleAuthorizationPlugin) {
        roles(roles.toSet())
    }

    route.build()
}

fun String?.toListOfIntOrNull() = takeIf { !it.isNullOrBlank() }?.run {
    val integerStrings = this.replace("[", "").replace("]", "").split(",")
    integerStrings.mapNotNull(String::toIntOrNull)
}

fun String?.toListOfStringOrNull() = takeIf { !it.isNullOrBlank() }?.run {
    val integerStrings = this.replace("[", "").replace("]", "").split(",")
    integerStrings.map(String::trim)
}

fun Parameters.extractString(key: String) = this[key]?.trim()?.takeIf { it.isNotEmpty() } ?: ""
