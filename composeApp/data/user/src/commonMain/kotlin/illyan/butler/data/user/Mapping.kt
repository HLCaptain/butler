package illyan.butler.data.user

import illyan.butler.core.local.room.mapping.toRoomModel
import illyan.butler.domain.model.Address
import illyan.butler.domain.model.Token
import illyan.butler.domain.model.User
import illyan.butler.shared.model.identity.AddressDto
import illyan.butler.shared.model.identity.UserDto
import illyan.butler.shared.model.response.UserTokensResponse

fun UserDto.toDomainModel(tokens: UserTokensResponse) = User(
    id = id!!,,
    email = email,
    username = username,
    displayName = displayName,
    phone = phone,
    fullName = fullName,
    photoUrl = photoUrl,
    address = address?.toDomainModel(),
    accessToken = Token(
        token = tokens.accessToken,
        tokenExpirationMillis = tokens.accessTokenExpirationMillis
    ),
    refreshToken = Token(
        token = tokens.refreshToken,
        tokenExpirationMillis = tokens.refreshTokenExpirationMillis
    )
)

fun AddressDto.toDomainModel() = Address(
    street = street,
    city = city,
    state = state,
    zip = zip
)

fun UserDto.toRoomModel(tokens: UserTokensResponse) = toDomainModel(tokens).toRoomModel()
