package ai.nest.api_gateway.plugins

import ai.nest.api_gateway.data.model.authenticate.TokenConfiguration
import ai.nest.api_gateway.endpoints.authenticationRoutes
import ai.nest.api_gateway.endpoints.chatRoute
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing

fun Application.configureRouting(tokenConfiguration: TokenConfiguration) {
    routing {
        authenticationRoutes(tokenConfiguration)
        chatRoute()
        get {
            call.respondText("Hello World!")
        }
    }
}
