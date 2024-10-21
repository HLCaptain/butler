package illyan.butler.data.mapping

import illyan.butler.data.network.model.ai.ModelDto
import illyan.butler.model.DomainModel

fun ModelDto.toDomainModel() = DomainModel(
    id = id,
    name = name,
    description = description,
    greetingMessage = greetingMessage,
    author = author
)

fun DomainModel.toNetworkModel() = ModelDto(
    id = id,
    name = name,
    description = description,
    greetingMessage = greetingMessage,
    author = author
)
