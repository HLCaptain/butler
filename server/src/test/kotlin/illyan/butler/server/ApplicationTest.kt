package illyan.butler.server

import illyan.butler.server.data.model.authenticate.TokenConfiguration
import illyan.butler.server.plugins.configureAuthentication
import illyan.butler.server.plugins.configureCompression
import illyan.butler.server.plugins.configureDependencyInjection
import illyan.butler.server.plugins.configureRouting
import illyan.butler.server.plugins.configureSerialization
import illyan.butler.server.plugins.configureStatusPages
import io.ktor.client.call.body
import io.ktor.client.plugins.compression.ContentEncoding
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.serialization.kotlinx.protobuf.protobuf
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import io.ktor.server.testing.testApplication
import kotlinx.serialization.ExperimentalSerializationApi
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
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
            configureStatusPages()
            configureCompression()
            configureRouting(tokenConfig)
        }
        val client = createClient {
            install(ContentNegotiation) {
                json()
                protobuf()
            }
            install(ContentEncoding) {
                gzip(1.0f)
                deflate(0.9f)
            }
        }
        client.get("/").apply {
            assertEquals(HttpStatusCode.OK, status)
            assertTrue {
                val enabledContentTypes = AppConfig.Ktor.SUPPORTED_CONTENT_TYPES.map { it.toString() } + ContentType.Text.Plain.toString()
                headers[HttpHeaders.ContentType]?.split(";")?.map { it.trim() }?.any {
                    enabledContentTypes.contains(it)
                } ?: false
            }
            assertEquals("Hello" to "World!", body())
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    @Test
    fun testLargePacket() = testApplication {
        val largeData = Random.nextBytes(10000000).toString()
        application {
            routing {
                get("/large-packet") {
                    call.respond(largeData)
                }
            }
        }
        val client = createClient {
            install(ContentNegotiation) {
                json()
                protobuf()
            }
            install(ContentEncoding) {
                gzip(1.0f)
                deflate(0.9f)
            }
        }
        client.get("/large-packet").apply {
            assertEquals(HttpStatusCode.OK, status)
            assertTrue {
                val enabledContentTypes = AppConfig.Ktor.SUPPORTED_CONTENT_TYPES.map { it.toString() } + ContentType.Text.Plain.toString()
                headers[HttpHeaders.ContentType]?.split(";")?.map { it.trim() }?.any {
                    enabledContentTypes.contains(it)
                } ?: false
            }
            assertEquals(largeData, body())
        }
    }
}
