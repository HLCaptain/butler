package illyan.butler.server.endpoints

import illyan.butler.server.data.service.IdentityService
import illyan.butler.server.utils.Claim
import illyan.butler.shared.model.auth.UserLoginDto
import illyan.butler.shared.model.auth.UserRegistrationDto
import illyan.butler.shared.model.authenticate.TokenConfiguration
import io.ktor.http.HttpStatusCode
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
