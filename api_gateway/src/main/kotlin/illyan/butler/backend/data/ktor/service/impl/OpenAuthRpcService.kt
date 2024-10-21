package illyan.butler.backend.data.ktor.service.impl

import illyan.butler.backend.data.ktor.service.open.OpenAuthService
import illyan.butler.backend.data.model.authenticate.TokenConfiguration
import illyan.butler.backend.data.model.authenticate.UserLoginResponseDto
import illyan.butler.backend.data.model.identity.UserLoginDto
import illyan.butler.backend.data.model.identity.UserRegistrationDto
import illyan.butler.backend.data.service.IdentityService
import org.koin.core.annotation.Factory
import org.koin.core.annotation.InjectedParam
import kotlin.coroutines.CoroutineContext

@Factory
class OpenAuthRpcService(
    @InjectedParam override val coroutineContext: CoroutineContext,
    @InjectedParam private val tokenConfiguration: TokenConfiguration,
    private val identityService: IdentityService
) : OpenAuthService {
    override suspend fun signup(credentials: UserRegistrationDto): UserLoginResponseDto {
        return identityService.createUser(credentials, tokenConfiguration)
    }

    override suspend fun login(credentials: UserLoginDto): UserLoginResponseDto {
        return identityService.loginUser(credentials.email, credentials.password, tokenConfiguration)
    }
}
