package ai.nest.api_gateway

import ai.nest.api_gateway.data.model.authenticate.TokenConfiguration
import ai.nest.api_gateway.plugins.configureAuthentication
import ai.nest.api_gateway.plugins.configureDependencyInjection
import ai.nest.api_gateway.plugins.configureMonitoring
import ai.nest.api_gateway.plugins.configureRouting
import ai.nest.api_gateway.plugins.configureSerialization
import ai.nest.api_gateway.plugins.configureStatusPages
import ai.nest.api_gateway.plugins.configureWebSockets
import io.ktor.server.application.Application
import io.ktor.server.netty.EngineMain
import kotlin.time.Duration.Companion.days

fun main(args: Array<String>) = EngineMain.main(args)

fun Application.module() {
    val jwtSecret = environment.config.property("jwt.secret").getString()
    val jwtIssuer = environment.config.property("jwt.issuer").getString()
    val jwtAudience = environment.config.property("jwt.audience").getString()

    val tokenConfig = TokenConfiguration(
        secret = jwtSecret,
        issuer = jwtIssuer,
        audience = jwtAudience,
        accessTokenExpirationTimestamp = 365.days.inWholeMilliseconds,
        refreshTokenExpirationTimestamp = 365.days.inWholeMilliseconds
    )

    configureMonitoring()
    configureDependencyInjection()
    configureAuthentication()
    configureSerialization()
    configureRouting(tokenConfig)
    configureStatusPages()
    configureWebSockets()
}
