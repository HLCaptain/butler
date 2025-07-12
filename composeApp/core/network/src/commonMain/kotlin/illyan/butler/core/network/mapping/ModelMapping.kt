package illyan.butler.core.network.mapping

import illyan.butler.domain.model.Model
import illyan.butler.shared.model.llm.ModelDto

fun ModelDto.toDomainModel() = Model(
    name = name,
    id = id,
    ownedBy = ownedBy,
    endpoint = endpoint
)

fun Model.toNetworkModel() = ModelDto(
    name = name,
    id = id,
    ownedBy = ownedBy,
    endpoint = endpoint
)
