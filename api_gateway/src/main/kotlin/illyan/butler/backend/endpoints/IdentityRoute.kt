package illyan.butler.backend.endpoints

import illyan.butler.backend.data.model.authenticate.TokenConfiguration
import illyan.butler.backend.data.model.identity.UserLoginDto
import illyan.butler.backend.data.model.identity.UserRegistrationDto
import illyan.butler.backend.data.service.IdentityService
import illyan.butler.backend.utils.Claim
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import org.koin.ktor.ext.inject

fun Route.identityRoutes(tokenConfiguration: TokenConfiguration) {
    val identityService: IdentityService by inject()

    post("/signup") {
        val newUser = call.receive<UserRegistrationDto>()
        val result = identityService.createUser(newUser, tokenConfiguration)
        call.respond(HttpStatusCode.Created, result)
    }

    post("/login") {
        val (email, password) = call.receive<UserLoginDto>()
        call.respond(HttpStatusCode.Accepted, identityService.loginUser(email, password, tokenConfiguration))
    }

    authenticate("auth-jwt") {
        post("/refresh-access-token") {
            val userId = call.principal<JWTPrincipal>()?.payload?.getClaim(Claim.USER_ID).toString().trim('\"', ' ')
            val token = identityService.generateUserTokens(userId, tokenConfiguration)
            call.respond(HttpStatusCode.Created, token)
        }
    }
}