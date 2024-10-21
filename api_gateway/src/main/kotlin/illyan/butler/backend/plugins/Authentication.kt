package illyan.butler.backend.plugins

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import illyan.butler.backend.AppConfig
import illyan.butler.backend.data.model.authenticate.TokenType
import illyan.butler.backend.utils.Claim
import io.ktor.http.auth.HttpAuthHeader
import io.ktor.server.application.Application
import io.ktor.server.auth.UnauthorizedResponse
import io.ktor.server.auth.authentication
import io.ktor.server.auth.jwt.JWTAuthenticationProvider
import io.ktor.server.auth.jwt.jwt
import io.ktor.server.response.respond

fun Application.configureAuthentication() {

    // Configuration file set from resources/application.yaml
    val jwtSecret = AppConfig.Jwt.SECRET
    val jwtDomain = AppConfig.Jwt.ISSUER
    val jwtAudience = AppConfig.Jwt.AUDIENCE
    val jwtRealm = AppConfig.Jwt.REALM

    authentication {
        jwt("auth-jwt") {
            realm = jwtRealm
            verifier(
                JWT.require(Algorithm.HMAC256(jwtSecret))
                    .withAudience(jwtAudience)
                    .withIssuer(jwtDomain)
                    .withClaim(Claim.TOKEN_TYPE, TokenType.ACCESS_TOKEN.name)
                    .withClaimPresence(Claim.USER_ID)
                    .build()
            )
            respondUnauthorized()
        }

        jwt("refresh-jwt") {
            realm = jwtRealm
            verifier(
                JWT.require(Algorithm.HMAC256(jwtSecret))
                    .withAudience(jwtAudience)
                    .withIssuer(jwtDomain)
                    .withClaim(Claim.TOKEN_TYPE, TokenType.REFRESH_TOKEN.name)
                    .withClaimPresence(Claim.USER_ID)
                    .build()
            )
            respondUnauthorized()
        }
    }
//    routing {
//        authenticate("auth-oauth-google") {
//            get("login") {
//                call.respondRedirect("/callback")
//            }
//
//            get("/callback") {
//                val principal: OAuthAccessTokenResponse.OAuth2? = call.authentication.principal()
//                call.sessions.set(UserSession(principal?.accessToken.toString()))
//                call.respondRedirect("/hello")
//            }
//        }
//    }
}

private fun JWTAuthenticationProvider.Config.respondUnauthorized() {
    challenge { scheme, realm ->
        call.respond(UnauthorizedResponse(HttpAuthHeader.bearerAuthChallenge(scheme, realm)))
    }
}
