package illyan.butler.data.model

import illyan.butler.domain.model.Model
import illyan.butler.shared.model.llm.ModelDto

fun ModelDto.toDomainModel() = Model(
    name = name,
    id = id,
    endpoint = endpoint,
    ownedBy = ownedBy,
)

fun Model.toNetworkModel() = ModelDto(
    name = name,
    id = id,
    endpoint = endpoint,
    ownedBy = ownedBy,
)
