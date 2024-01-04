package ai.nest.api_gateway

import ai.nest.api_gateway.plugins.configureRouting
import ai.nest.api_gateway.plugins.configureSecurity
import io.ktor.server.application.Application
import io.ktor.server.netty.EngineMain

fun main(args: Array<String>) = EngineMain.main(args)

fun Application.module() {
    configureSecurity()
    configureRouting()
}
