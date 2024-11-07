package illyan.butler.data.model

import illyan.butler.domain.model.DomainModel
import illyan.butler.shared.model.llm.ModelDto

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
