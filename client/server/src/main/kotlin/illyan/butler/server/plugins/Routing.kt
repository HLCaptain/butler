package illyan.butler.server.plugins

import illyan.butler.server.endpoints.aiRoute
import illyan.butler.server.endpoints.chatRoute
import illyan.butler.server.endpoints.identityRoutes
import illyan.butler.shared.model.authenticate.TokenConfiguration
import io.ktor.server.application.Application
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import kotlin.random.Random

fun Application.configureRouting(tokenConfiguration: TokenConfiguration) {
    routing {
        identityRoutes(tokenConfiguration)
        chatRoute()
        aiRoute()
        get {
            call.respond("Hello" to "World!")
        }
        get("/large-packet") {
            val largeData = (1..1000).map { Random.nextInt(0, 100000).toString() }
            call.respond(largeData)
        }
    }
}
