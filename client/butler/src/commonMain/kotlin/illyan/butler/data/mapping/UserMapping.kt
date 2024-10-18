package illyan.butler.data.mapping

import illyan.butler.data.network.model.auth.UserTokensResponse
import illyan.butler.data.network.model.identity.AddressDto
import illyan.butler.data.network.model.identity.UserDto
import illyan.butler.data.local.room.model.RoomUser
import illyan.butler.domain.model.DomainAddress
import illyan.butler.domain.model.DomainToken
import illyan.butler.domain.model.DomainUser

fun RoomUser.toDomainModel() = DomainUser(
    id = id,
    email = email,
    username = username,
    displayName = displayName,
    phone = phone,
    fullName = fullName,
    photoUrl = photoUrl,
    address = address,
    accessToken = accessToken,
    refreshToken = refreshToken
)

fun DomainUser.toNetworkModel() = UserDto(
    id = id,
    email = email,
    username = username,
    displayName = displayName,
    phone = phone,
    fullName = fullName,
    photoUrl = photoUrl,
    address = address?.toNetworkModel()
)

fun UserDto.toDomainModel(
    tokensResponse: UserTokensResponse? = null
) = DomainUser(
    id = id,
    email = email,
    username = username,
    displayName = displayName,
    phone = phone,
    fullName = fullName,
    photoUrl = photoUrl,
    address = address?.toDomainModel(),
    accessToken = tokensResponse?.let { DomainToken(it.accessToken, it.accessTokenExpirationMillis) },
    refreshToken = tokensResponse?.let { DomainToken(it.refreshToken, it.refreshTokenExpirationMillis) }
)

fun DomainUser.toRoomModel() = RoomUser(
    id = id,
    email = email,
    username = username,
    displayName = displayName,
    phone = phone,
    fullName = fullName,
    photoUrl = photoUrl,
    address = address,
    accessToken = accessToken,
    refreshToken = refreshToken
)

fun RoomUser.toNetworkModel() = toDomainModel().toNetworkModel()
fun UserDto.toRoomModel(tokensResponse: UserTokensResponse? = null) = toDomainModel(tokensResponse).toRoomModel()

fun AddressDto.toDomainModel() = DomainAddress(
    street = street,
    city = city,
    state = state,
    zip = zip
)

fun DomainAddress.toNetworkModel() = AddressDto(
    street = street,
    city = city,
    state = state,
    zip = zip
)
