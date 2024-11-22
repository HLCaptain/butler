package illyan.butler.core.network.mapping

import illyan.butler.domain.model.DomainResource
import illyan.butler.shared.model.chat.ResourceDto

fun ResourceDto.toDomainModel() = DomainResource(
    id = id,
    type = type,
    data = data
)

fun DomainResource.toNetworkModel() = ResourceDto(
    id = id,
    type = type,
    data = data
)
