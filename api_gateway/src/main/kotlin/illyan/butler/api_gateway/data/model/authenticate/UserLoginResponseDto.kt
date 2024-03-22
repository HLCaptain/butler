package illyan.butler.api_gateway.data.model.authenticate

import illyan.butler.api_gateway.data.model.identity.UserDto
import illyan.butler.api_gateway.data.model.response.UserTokensResponse
import kotlinx.serialization.Serializable

@Serializable
data class UserLoginResponseDto(
    val user: UserDto,
    val tokensResponse: UserTokensResponse
)
