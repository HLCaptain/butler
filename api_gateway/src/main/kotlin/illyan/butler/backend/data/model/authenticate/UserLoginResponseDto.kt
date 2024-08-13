package illyan.butler.backend.data.model.authenticate

import illyan.butler.backend.data.model.identity.UserDto
import illyan.butler.backend.data.model.response.UserTokensResponse
import kotlinx.serialization.Serializable

@Serializable
data class UserLoginResponseDto(
    val user: UserDto,
    val tokensResponse: UserTokensResponse
)
