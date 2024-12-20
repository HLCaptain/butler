package illyan.butler.data.model

import illyan.butler.domain.model.DomainModel
import illyan.butler.shared.model.llm.ModelDto

fun ModelDto.toDomainModel() = DomainModel(
    name = name,
    id = id,
    endpoint = endpoint,
    ownedBy = ownedBy,
)

fun DomainModel.toNetworkModel() = ModelDto(
    name = name,
    id = id,
    endpoint = endpoint,
    ownedBy = ownedBy,
)
