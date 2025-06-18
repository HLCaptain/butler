package illyan.butler.server.plugins

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import illyan.butler.server.AppConfig
import illyan.butler.server.utils.Claim
import illyan.butler.shared.model.authenticate.TokenType
import io.github.aakira.napier.Napier
import io.ktor.http.auth.HttpAuthHeader
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.UnauthorizedResponse
import io.ktor.server.auth.authentication
import io.ktor.server.auth.jwt.JWTAuthenticationProvider
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.jwt.jwt
import io.ktor.server.response.respond

fun Application.configureAuthentication() {

    // Configuration file set from resources/application.conf
    val jwtSecret = AppConfig.Jwt.SECRET
    val jwtDomain = AppConfig.Jwt.ISSUER
    val jwtAudience = AppConfig.Jwt.AUDIENCE
    val jwtRealm = AppConfig.Jwt.REALM

    install(Authentication) {
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
            validate()
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
            validate()
            respondUnauthorized()
        }
    }
}

private fun JWTAuthenticationProvider.Config.validate() {
    validate {
        val userId = it.payload.getClaim(Claim.USER_ID).asString().trim('\"', ' ')
        if (userId.isNotEmpty()) {
            Napier.d { "JWT authentication successful for userId: $userId" }
            JWTPrincipal(it.payload)
        } else {
            Napier.e { "JWT authentication failed: User ID is missing" }
            null
        }
    }
}

private fun JWTAuthenticationProvider.Config.respondUnauthorized() {
    challenge { scheme, realm ->
        Napier.e {
            "Unauthorized request: scheme=$scheme, realm=$realm, " +
            "authentication=${call.authentication.principal<JWTAuthenticationProvider>()}"
        }
        call.respond(UnauthorizedResponse(HttpAuthHeader.bearerAuthChallenge(scheme, realm)))
    }
}
