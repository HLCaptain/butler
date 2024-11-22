package illyan.butler.data.user

import illyan.butler.core.local.room.mapping.toRoomModel
import illyan.butler.domain.model.DomainAddress
import illyan.butler.domain.model.DomainToken
import illyan.butler.domain.model.DomainUser
import illyan.butler.shared.model.identity.AddressDto
import illyan.butler.shared.model.identity.UserDto
import illyan.butler.shared.model.response.UserTokensResponse

fun UserDto.toDomainModel(tokens: UserTokensResponse) = DomainUser(
    id = id!!,
    email = email,
    username = username,
    displayName = displayName,
    phone = phone,
    fullName = fullName,
    photoUrl = photoUrl,
    address = address?.toDomainModel(),
    refreshToken = DomainToken(
        token = tokens.refreshToken,
        tokenExpirationMillis = tokens.refreshTokenExpirationMillis
    ),
    accessToken = DomainToken(
        token = tokens.accessToken,
        tokenExpirationMillis = tokens.accessTokenExpirationMillis
    )
)

fun AddressDto.toDomainModel() = DomainAddress(
    street = street,
    city = city,
    state = state,
    zip = zip
)

fun UserDto.toRoomModel(tokens: UserTokensResponse) = toDomainModel(tokens).toRoomModel()
