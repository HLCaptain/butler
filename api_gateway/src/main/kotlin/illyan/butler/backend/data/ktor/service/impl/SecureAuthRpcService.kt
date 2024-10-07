package illyan.butler.backend.data.ktor.service.impl

import com.auth0.jwt.interfaces.Payload
import illyan.butler.backend.data.ktor.service.secure.SecureAuthService
import illyan.butler.backend.data.model.authenticate.TokenConfiguration
import illyan.butler.backend.data.model.response.UserTokensResponse
import illyan.butler.backend.data.service.IdentityService
import illyan.butler.backend.utils.Claim
import org.koin.core.annotation.Factory
import org.koin.core.annotation.InjectedParam
import kotlin.coroutines.CoroutineContext

@Factory
class SecureAuthRpcService(
    @InjectedParam override val coroutineContext: CoroutineContext,
    @InjectedParam override val jwtPayload: Payload,
    @InjectedParam private val tokenConfiguration: TokenConfiguration,
    private val identityService: IdentityService
) : SecureAuthService {
    override suspend fun refreshUserTokens(): UserTokensResponse {
        val userId = jwtPayload.claims[Claim.USER_ID]!!.asString()
        return identityService.generateUserTokens(userId, tokenConfiguration)
    }
}
