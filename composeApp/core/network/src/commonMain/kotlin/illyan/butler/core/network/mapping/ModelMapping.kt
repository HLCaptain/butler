package illyan.butler.core.network.mapping

import illyan.butler.domain.model.DomainModel
import illyan.butler.shared.model.llm.ModelDto

fun ModelDto.toDomainModel() = DomainModel(
    name = name,
    id = id,
    description = description,
    greetingMessage = greetingMessage,
    author = author
)

fun DomainModel.toNetworkModel() = ModelDto(
    name = name,
    id = id,
    description = description,
    greetingMessage = greetingMessage,
    author = author
)
