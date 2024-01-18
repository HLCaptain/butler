package ai.nest

import ai.nest.api_gateway.data.model.authenticate.TokenConfiguration
import ai.nest.api_gateway.plugins.configureRouting
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.testApplication
import kotlin.test.Test
import kotlin.test.assertEquals
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
                accessTokenExpireDuration = 365.days,
                refreshTokenExpireDuration = 365.days
            )
            configureRouting(tokenConfig)
        }
        client.get("/").apply {
            assertEquals(HttpStatusCode.OK, status)
            assertEquals("Hello World!", bodyAsText())
        }
    }
}
