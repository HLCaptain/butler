package ai.nest.api_gateway.plugins

import ai.nest.Butler_API_Gateway.BuildConfig
import ai.nest.api_gateway.data.model.authenticate.TokenConfiguration
import ai.nest.api_gateway.endpoints.authenticationRoutes
import ai.nest.api_gateway.endpoints.chatRoute
import ai.nest.api_gateway.endpoints.utils.ContentVersion
import io.ktor.http.HttpHeaders
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.plugins.defaultheaders.DefaultHeaders
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.routing

fun Application.configureRouting(tokenConfiguration: TokenConfiguration) {
    install(DefaultHeaders) {
        // No need to check Accept-Version header, as it is not used in the code
        // Kubernetes will handle ingress based on the version
        header(HttpHeaders.ContentVersion, BuildConfig.API_VERSION)
    }
    routing {
        authenticationRoutes(tokenConfiguration)
        chatRoute()
        get {
            call.respond("Hello" to "World!")
        }
    }
}
