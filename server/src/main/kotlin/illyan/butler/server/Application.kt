package illyan.butler.server

import illyan.butler.server.plugins.configureAuthentication
import illyan.butler.server.plugins.configureCompression
import illyan.butler.server.plugins.configureDependencyInjection
import illyan.butler.server.plugins.configureMonitoring
import illyan.butler.server.plugins.configureRouting
import illyan.butler.server.plugins.configureSerialization
import illyan.butler.server.plugins.configureStatusPages
import illyan.butler.server.plugins.confirugreSSE
import illyan.butler.shared.model.authenticate.TokenConfiguration
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier
import io.ktor.server.application.Application
import io.ktor.server.cio.CIO
import io.ktor.server.engine.embeddedServer
import kotlin.time.Duration.Companion.days

fun main() {
    // Configure Ktor Server developmentMode
    System.setProperty("io.ktor.development", AppConfig.Ktor.DEVELOPMENT.toString())

    embeddedServer(
        factory = CIO,
        port = AppConfig.Ktor.PORT,
        module = Application::module,
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
    configureStatusPages()
    configureCompression()
    configureRouting(tokenConfig)
    confirugreSSE()
}
