@file:OptIn(ExperimentalUuidApi::class, ExperimentalTime::class)

package illyan.butler.core.network.mapping

import illyan.butler.domain.model.Address
import illyan.butler.domain.model.User
import illyan.butler.shared.model.identity.AddressDto
import illyan.butler.shared.model.identity.UserDto
import kotlin.time.ExperimentalTime
import kotlin.uuid.ExperimentalUuidApi

fun UserDto.toDomainModel(
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
    filters = filters
)

fun AddressDto.toDomainModel() = Address(
    street = street,
    city = city,
    state = state,
    zip = zip
)

fun User.toNetworkModel() = UserDto(
    id = id,
    email = email,
    username = username,
    displayName = displayName,
    phone = phone,
    fullName = fullName,
    photoUrl = photoUrl,
    address = address?.toNetworkModel(),
    filters = filters
)

fun Address.toNetworkModel() = AddressDto(
    street = street,
    city = city,
    state = state,
    zip = zip
)
