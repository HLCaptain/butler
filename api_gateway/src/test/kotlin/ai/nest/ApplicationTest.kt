package ai.nest

import ai.nest.api_gateway.data.model.authenticate.TokenConfiguration
import ai.nest.api_gateway.plugins.configureAuthentication
import ai.nest.api_gateway.plugins.configureDependencyInjection
import ai.nest.api_gateway.plugins.configureRouting
import ai.nest.api_gateway.plugins.configureSerialization
import ai.nest.api_gateway.plugins.configureStatusPages
import ai.nest.api_gateway.plugins.configureWebSockets
import ai.nest.api_gateway.utils.AppConfig
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.serialization.kotlinx.protobuf.protobuf
import io.ktor.server.testing.testApplication
import kotlinx.serialization.ExperimentalSerializationApi
import org.koin.ktor.ext.get
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.days

class ApplicationTest {
    @OptIn(ExperimentalSerializationApi::class)
    @Test
    fun testRoot() = testApplication {
        application {
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
            configureDependencyInjection()
            configureAuthentication()
            configureSerialization()
            configureWebSockets(get())
            configureStatusPages()
            configureRouting(tokenConfig)
        }
        val client = createClient {
            install(ContentNegotiation) {
                json()
                protobuf()
            }
        }
        AppConfig.Ktor.SUPPORTED_CONTENT_TYPES.forEach {
            client.get("/") {
                header(HttpHeaders.ContentType, it)
            }.apply {
                assertEquals(HttpStatusCode.OK, status)
                assertEquals("Hello" to "World!", body())
            }
        }
    }
}
