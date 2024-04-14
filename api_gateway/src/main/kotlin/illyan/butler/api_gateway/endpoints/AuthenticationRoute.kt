package illyan.butler.api_gateway.endpoints

import illyan.butler.api_gateway.data.model.authenticate.TokenConfiguration
import illyan.butler.api_gateway.data.model.identity.UserLoginDto
import illyan.butler.api_gateway.data.model.identity.UserRegistrationDto
import illyan.butler.api_gateway.data.service.IdentityService
import illyan.butler.api_gateway.endpoints.utils.WebSocketServerHandler
import illyan.butler.api_gateway.utils.Claim
import io.github.aakira.napier.Napier
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import io.ktor.server.websocket.webSocket
import org.koin.ktor.ext.inject

fun Route.authenticationRoutes(tokenConfiguration: TokenConfiguration) {
    val identityService: IdentityService by inject()
    val webSocketServerHandler: WebSocketServerHandler by inject()

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
        webSocket("/me") {
            val tokenClaim = call.principal<JWTPrincipal>()
            val id = tokenClaim?.payload?.getClaim(Claim.USER_ID).toString().trim('\"', ' ')
            webSocketServerHandler.addFlowSessionListener("me:$id", this) {
                identityService.getUserChangesById(id)
            }
            Napier.d("Added user listener for $id")
        }

        post("/refresh-access-token") {
            val payload = call.principal<JWTPrincipal>()?.payload
            val userId = payload?.getClaim(Claim.USER_ID).toString().trim('\"', ' ')
            val token = identityService.generateUserTokens(userId, tokenConfiguration)
            call.respond(HttpStatusCode.Created, token)
        }
    }
}