package illyan.butler.services.chat

import ai.nest.plugins.*
import illyan.butler.services.chat.plugins.configureDatabases
import illyan.butler.services.chat.plugins.configureHTTP
import illyan.butler.services.chat.plugins.configureMonitoring
import illyan.butler.services.chat.plugins.configureRouting
import illyan.butler.services.chat.plugins.configureSerialization
import illyan.butler.services.chat.plugins.configureSockets
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    configureSockets()
    configureSerialization()
    configureDatabases()
    configureHTTP()
    configureMonitoring()
    configureRouting()
}
