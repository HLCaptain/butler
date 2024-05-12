package illyan.butler.data.mapping

import illyan.butler.data.network.model.chat.ResourceDto
import illyan.butler.db.Resource
import illyan.butler.domain.model.DomainResource

fun Resource.toDomainModel() = DomainResource(
    id = id,
    type = type,
    data = data_,
)

fun DomainResource.toNetworkModel() = ResourceDto(
    id = id,
    type = type,
    data = data,
)

fun ResourceDto.toLocalModel() = Resource(
    id = id!!,
    type = type,
    data_ = data,
)

fun Resource.toNetworkModel() = toDomainModel().toNetworkModel()
fun DomainResource.toLocalModel() = toNetworkModel().toLocalModel()
fun ResourceDto.toDomainModel() = toLocalModel().toDomainModel()