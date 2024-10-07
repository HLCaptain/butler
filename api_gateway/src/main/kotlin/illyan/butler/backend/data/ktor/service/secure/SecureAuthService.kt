package illyan.butler.backend.data.ktor.service.secure

import illyan.butler.backend.data.model.response.UserTokensResponse

interface SecureAuthService : RPCWithJWT {
    suspend fun refreshUserTokens(): UserTokensResponse
}