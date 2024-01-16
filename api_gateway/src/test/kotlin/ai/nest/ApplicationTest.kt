package ai.nest

import ai.nest.api_gateway.data.model.authenticate.TokenConfiguration
import ai.nest.api_gateway.plugins.configureRouting
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlin.test.*
import kotlin.time.Duration.Companion.days

class ApplicationTest {
    @Test
    fun testRoot() = testApplication {
        application {
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
            configureRouting(tokenConfig)
        }
        client.get("/").apply {
            assertEquals(HttpStatusCode.OK, status)
            assertEquals("Hello World!", bodyAsText())
        }
    }
}
