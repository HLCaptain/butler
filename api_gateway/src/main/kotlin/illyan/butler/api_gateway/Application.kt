package illyan.butler.api_gateway

import illyan.butler.api_gateway.data.model.authenticate.TokenConfiguration
import illyan.butler.api_gateway.plugins.configureAuthentication
import illyan.butler.api_gateway.plugins.configureCompression
import illyan.butler.api_gateway.plugins.configureDependencyInjection
import illyan.butler.api_gateway.plugins.configureMonitoring
import illyan.butler.api_gateway.plugins.configureRouting
import illyan.butler.api_gateway.plugins.configureSerialization
import illyan.butler.api_gateway.plugins.configureStatusPages
import illyan.butler.api_gateway.plugins.configureWebSockets
import illyan.butler.api_gateway.utils.AppConfig
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier
import io.ktor.server.application.Application
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import kotlin.time.Duration.Companion.days

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

    if (AppConfig.Ktor.DEVELOPMENT) {
        Napier.base(DebugAntilog())
    }

    configureMonitoring()
    configureDependencyInjection()
    configureAuthentication()
    configureSerialization()
    configureWebSockets()
    configureStatusPages()
    configureCompression()
    configureRouting(tokenConfig)
}
