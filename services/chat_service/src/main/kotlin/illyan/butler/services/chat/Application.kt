package illyan.butler.services.chat

import illyan.butler.services.chat.plugins.configureCompression
import illyan.butler.services.chat.plugins.configureMonitoring
import illyan.butler.services.chat.plugins.configureRouting
import illyan.butler.services.chat.plugins.configureSerialization
import illyan.butler.services.chat.plugins.configureWebSockets
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
    configureCompression()
    configureSerialization()
    configureWebSockets()
    configureRouting()
}
