package ai.nest.api_gateway.endpoints

import ai.nest.api_gateway.data.model.authenticate.TokenConfiguration
import ai.nest.api_gateway.data.model.identity.UserRegistrationDto
import ai.nest.api_gateway.data.service.IdentityService
import ai.nest.api_gateway.data.service.NotificationService
import ai.nest.api_gateway.data.utils.LocalizedMessagesFactory
import ai.nest.api_gateway.endpoints.utils.extractApplicationIdHeader
import ai.nest.api_gateway.endpoints.utils.extractLocalizationHeader
import ai.nest.api_gateway.endpoints.utils.respondWithResult
import ai.nest.api_gateway.endpoints.utils.withRoles
import ai.nest.api_gateway.utils.Claim
import ai.nest.api_gateway.utils.Role
import com.auth0.jwt.JWT
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.request.receiveParameters
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import kotlinx.coroutines.async
import org.koin.ktor.ext.inject

fun Route.authenticationRoutes(tokenConfiguration: TokenConfiguration) {
    val identityService: IdentityService by inject()
    val notificationService: NotificationService by inject()

    val localizedMessagesFactory: LocalizedMessagesFactory by inject()

    post("/signup") {
        val newUser = call.receive<UserRegistrationDto>()
        val language = extractLocalizationHeader()

        val result = identityService.createUser(newUser, language)
        val successMessage = localizedMessagesFactory.createLocalizedMessages(language).userCreatedSuccessfully
        respondWithResult(HttpStatusCode.Created, result, successMessage)
    }

    withRoles(Role.END_USER) {
        post("/login") {
            val params = call.receiveParameters()
            val userName = params["username"]?.trim().toString()
            val password = params["password"]?.trim().toString()
            val deviceToken = params["token"]?.trim()

            val language = extractLocalizationHeader()
            val appId = extractApplicationIdHeader()
            val token =
                async { identityService.loginUser(userName, password, tokenConfiguration, language, appId) }.await()

            respondWithResult(HttpStatusCode.OK, token)

            if (!deviceToken.isNullOrBlank()) {
                val jwt = JWT.decode(token.accessToken)
                val userId = jwt.getClaim(Claim.USER_ID).asString()
                notificationService.saveToken(userId, deviceToken, language)
            }
        }

        get("/me") {
            val tokenClaim = call.principal<JWTPrincipal>()
            val id = tokenClaim?.payload?.getClaim(Claim.USER_ID).toString()
            respondWithResult(HttpStatusCode.OK, id)
        }

        post("/refresh-access-token") {
            val tokenClaim = call.principal<JWTPrincipal>()
            val userId = tokenClaim?.payload?.getClaim(Claim.USER_ID).toString()
            val username = tokenClaim?.payload?.getClaim(Claim.USERNAME).toString()
            val userPermission = tokenClaim?.payload?.getClaim(Claim.PERMISSION)?.`as`(Role::class.java) ?: Role.END_USER
            val token = identityService.generateUserTokens(userId, username, userPermission, tokenConfiguration)
            respondWithResult(HttpStatusCode.Created, token)
        }
    }
}