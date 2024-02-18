package ai.nest.api_gateway.endpoints

import ai.nest.api_gateway.data.model.authenticate.TokenConfiguration
import ai.nest.api_gateway.data.model.identity.UserLoginDto
import ai.nest.api_gateway.data.model.identity.UserRegistrationDto
import ai.nest.api_gateway.data.service.IdentityService
import ai.nest.api_gateway.endpoints.utils.respondWithResult
import ai.nest.api_gateway.utils.Claim
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

    post("/signup") {
        val newUser = call.receive<UserRegistrationDto>()
        val result = identityService.createUser(newUser)
        respondWithResult(HttpStatusCode.Created, result)
    }

    post("/login") {
        val (email, password) = call.receive<UserLoginDto>()
        val token = async { identityService.loginUser(email, password, tokenConfiguration) }.await()
        respondWithResult(HttpStatusCode.OK, token)
    }

    get("/me") {
        val tokenClaim = call.principal<JWTPrincipal>()
        val id = tokenClaim?.payload?.getClaim(Claim.USER_ID).toString()
        respondWithResult(HttpStatusCode.OK, id)
    }

    post("/refresh-access-token") {
        val payload = call.principal<JWTPrincipal>()?.payload
        val userId = payload?.getClaim(Claim.USER_ID).toString()
        val token = identityService.generateUserTokens(userId, tokenConfiguration)
        respondWithResult(HttpStatusCode.Created, token)
    }
}