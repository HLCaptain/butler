package illyan.butler.services.identity

import illyan.butler.services.identity.plugins.configureCompression
import illyan.butler.services.identity.plugins.configureDependencyInjection
import illyan.butler.services.identity.plugins.configureMonitoring
import illyan.butler.services.identity.plugins.configureRouting
import illyan.butler.services.identity.plugins.configureSerialization
import illyan.butler.services.identity.plugins.configureWebSockets
import io.ktor.server.application.Application
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty

fun main() {
    // Configure Ktor Server developmentMode
    System.setProperty("io.ktor.development", AppConfig.Ktor.DEVELOPMENT.toString())

    embeddedServer(
        factory = Netty,
        port = AppConfig.Ktor.PORT,
        module = Application::module
    ).start(wait = true)
}

fun Application.module() {
    configureMonitoring()
    configureDependencyInjection()
    configureCompression()
    configureSerialization()
    configureWebSockets()
    configureRouting()
}
