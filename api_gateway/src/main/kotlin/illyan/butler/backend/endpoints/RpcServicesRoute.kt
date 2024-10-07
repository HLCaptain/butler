package illyan.butler.backend.endpoints

import illyan.butler.backend.data.ktor.service.impl.OpenAuthRpcService
import illyan.butler.backend.data.ktor.service.impl.SecureAuthRpcService
import illyan.butler.backend.data.model.authenticate.TokenConfiguration
import illyan.butler.backend.utils.Claim
import io.github.aakira.napier.Napier
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.routing.Route
import kotlinx.rpc.transport.ktor.server.rpc
import org.koin.core.parameter.parametersOf
import org.koin.ktor.ext.getKoin

fun Route.rpcServicesRoute(tokenConfiguration: TokenConfiguration) {
    // TODO: handle correct authentication and authorization
    authenticate("auth-jwt") {
        rpc("/open") {
            val payload = call.principal<JWTPrincipal>()!!.payload
            Napier.d("RPC call to /services with userId: ${payload.getClaim(Claim.USER_ID).asString()}")
            registerService { ctx -> getKoin().get<SecureAuthRpcService> { parametersOf(ctx, payload, tokenConfiguration) } }
        }
    }
    rpc("/secure") {
        registerService { ctx -> getKoin().get<OpenAuthRpcService> { parametersOf(ctx, tokenConfiguration) } }
    }
}