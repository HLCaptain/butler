@file:OptIn(ExperimentalUuidApi::class, ExperimentalTime::class)

package illyan.butler.data.user

import illyan.butler.domain.model.Address
import illyan.butler.domain.model.Token
import illyan.butler.domain.model.User
import illyan.butler.shared.model.identity.AddressDto
import illyan.butler.shared.model.identity.UserDto
import illyan.butler.shared.model.response.UserTokensResponse
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import kotlin.uuid.ExperimentalUuidApi

fun UserDto.toDomainModel(
    tokens: UserTokensResponse,
    endpoint: String,
) = User(
    id = id,
    endpoint = endpoint,
    email = email,
    username = username,
    displayName = displayName,
    phone = phone,
    fullName = fullName,
    photoUrl = photoUrl,
    address = address?.toDomainModel(),
    accessToken = Token(
        token = tokens.accessToken,
        tokenExpiration = Instant.fromEpochMilliseconds(tokens.accessTokenExpirationMillis)
    ),
    refreshToken = Token(
        token = tokens.refreshToken,
        tokenExpiration = Instant.fromEpochMilliseconds(tokens.refreshTokenExpirationMillis)
    ),
)

fun AddressDto.toDomainModel() = Address(
    street = street,
    city = city,
    state = state,
    zip = zip
)
