package ai.nest.api_gateway.endpoints

import ai.nest.api_gateway.data.model.authenticate.TokenConfiguration
import ai.nest.api_gateway.data.model.identity.UserRegistrationDto
import ai.nest.api_gateway.data.service.IdentityService
import ai.nest.api_gateway.data.service.NotificationService
import ai.nest.api_gateway.endpoints.utils.extractApplicationIdHeader
import ai.nest.api_gateway.endpoints.utils.respondWithError
import ai.nest.api_gateway.endpoints.utils.respondWithResult
import ai.nest.api_gateway.endpoints.utils.withPermissions
import ai.nest.api_gateway.utils.Claim
import ai.nest.api_gateway.utils.Permission
import com.auth0.jwt.JWT
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import kotlinx.coroutines.async
import org.koin.ktor.ext.inject

fun Route.authenticationRoutes(tokenConfiguration: TokenConfiguration) {
    val identityService: IdentityService by inject()
    val notificationService: NotificationService by inject()

    post("/signup") {
        val newUser = call.receive<UserRegistrationDto>()
        val result = identityService.createUser(newUser)
        respondWithResult(HttpStatusCode.Created, result)
    }

    withPermissions(Permission.END_USER) {
        post("/login") {
            val userName = call.parameters["username"]?.trim().toString()
            val password = call.parameters["password"]?.trim().toString()
            val deviceToken = call.parameters["token"]?.trim()

            val appId = extractApplicationIdHeader()
            val token = async { identityService.loginUser(userName, password, tokenConfiguration, appId) }.await()

            respondWithResult(HttpStatusCode.OK, token)

            if (!deviceToken.isNullOrBlank()) {
                val jwt = JWT.decode(token.accessToken)
                val userId = jwt.getClaim(Claim.USER_ID).asString()
                notificationService.saveToken(userId, deviceToken)
            }
        }

        get("/me") {
            val tokenClaim = call.principal<JWTPrincipal>()
            val id = tokenClaim?.payload?.getClaim(Claim.USER_ID).toString()
            respondWithResult(HttpStatusCode.OK, id)
        }

        post("/refresh-access-token") {
            val payload = call.principal<JWTPrincipal>()?.payload
            val userId = payload?.getClaim(Claim.USER_ID).toString()
            val username = payload?.getClaim(Claim.USERNAME).toString()
            val userPermission = payload?.getClaim(Claim.PERMISSIONS)?.asList(Permission::class.java)?.toSet()
            if (userPermission != null) {
                val token = identityService.generateUserTokens(userId, username, userPermission, tokenConfiguration)
                respondWithResult(HttpStatusCode.Created, token)
            } else {
                respondWithError(call, HttpStatusCode.Unauthorized)
            }
        }
    }
}