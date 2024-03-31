package illyan.butler.services.ai.plugins

import illyan.butler.services.ai.BuildConfig
import illyan.butler.services.ai.endpoints.chatRoute
import io.ktor.http.HttpHeaders
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.plugins.defaultheaders.DefaultHeaders
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import kotlin.random.Random

fun Application.configureRouting() {
    install(DefaultHeaders) {
        // No need to check Accept-Version header, as it is not used in the code
        // Kubernetes will handle ingress based on the version
        header(HttpHeaders.ContentVersion, BuildConfig.API_VERSION)
    }
    routing {
        chatRoute()
        get {
            call.respond("Hello" to "World!")
        }
        get("/large-packet") {
            val largeData = (1..1000).map { Random.nextInt(0, 100000).toString() }
            call.respond(largeData)
        }
    }
}

val HttpHeaders.ContentVersion: String
    get() = "Content-Version"
