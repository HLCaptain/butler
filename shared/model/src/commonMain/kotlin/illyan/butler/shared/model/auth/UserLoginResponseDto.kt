package illyan.butler.shared.model.auth

import illyan.butler.shared.model.identity.UserDto
import illyan.butler.shared.model.response.UserTokensResponse
import kotlinx.serialization.Serializable

@Serializable
data class UserLoginResponseDto(
    val user: UserDto,
    val tokensResponse: UserTokensResponse
)
