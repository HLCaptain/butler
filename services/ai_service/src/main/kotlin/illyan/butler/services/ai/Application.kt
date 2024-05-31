package illyan.butler.services.ai

import illyan.butler.services.ai.plugins.configureCompression
import illyan.butler.services.ai.plugins.configureDependencyInjection
import illyan.butler.services.ai.plugins.configureMonitoring
import illyan.butler.services.ai.plugins.configureRouting
import illyan.butler.services.ai.plugins.configureSerialization
import illyan.butler.services.ai.plugins.configureWebSockets
import io.ktor.server.application.Application
import io.ktor.server.cio.CIO
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty

fun main() {
    // Configure Ktor Server developmentMode
    System.setProperty("io.ktor.development", AppConfig.Ktor.DEVELOPMENT.toString())

    embeddedServer(
        factory = CIO,
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
