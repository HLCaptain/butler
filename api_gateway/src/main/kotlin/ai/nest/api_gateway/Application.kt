package ai.nest.api_gateway

import ai.nest.api_gateway.data.model.authenticate.TokenConfiguration
import ai.nest.api_gateway.plugins.configureAuthentication
import ai.nest.api_gateway.plugins.configureDependencyInjection
import ai.nest.api_gateway.plugins.configureMonitoring
import ai.nest.api_gateway.plugins.configureRouting
import ai.nest.api_gateway.plugins.configureSerialization
import ai.nest.api_gateway.plugins.configureStatusPages
import ai.nest.api_gateway.plugins.configureWebSockets
import ai.nest.api_gateway.utils.AppConfig
import io.ktor.server.application.Application
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import org.koin.ktor.ext.get
import kotlin.time.Duration.Companion.days

fun main() {
    embeddedServer(
        factory = Netty,
        port = AppConfig.Ktor.PORT,
        module = Application::module
    ).start(wait = true)
}

fun Application.module() {
    val jwtSecret = AppConfig.Jwt.SECRET
    val jwtIssuer = AppConfig.Jwt.ISSUER
    val jwtAudience = AppConfig.Jwt.AUDIENCE

    val tokenConfig = TokenConfiguration(
        secret = jwtSecret,
        issuer = jwtIssuer,
        audience = jwtAudience,
        accessTokenExpireDuration = 365.days,
        refreshTokenExpireDuration = 365.days
    )

    configureMonitoring()
    configureDependencyInjection()
    configureAuthentication()
    configureSerialization()
    configureWebSockets(get())
    configureStatusPages()
    configureRouting(tokenConfig)
}
