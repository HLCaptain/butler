package illyan.butler.data.network.model.auth

import illyan.butler.data.network.model.identity.UserDto
import kotlinx.serialization.Serializable

@Serializable
data class UserLoginResponseDto(
    val user: UserDto,
    val tokensResponse: UserTokensResponse
)
