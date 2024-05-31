package illyan.butler.data.mapping

import illyan.butler.data.network.model.ai.ModelDto
import illyan.butler.db.Model
import illyan.butler.domain.model.DomainModel

fun Model.toDomainModel() = DomainModel(
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

fun ModelDto.toLocalModel() = Model(
    id = id,
    name = name,
    description = description,
    greetingMessage = greetingMessage,
    author = author
)

fun Model.toNetworkModel() = toDomainModel().toNetworkModel()
fun DomainModel.toLocalModel() = toNetworkModel().toLocalModel()
fun ModelDto.toDomainModel() = toLocalModel().toDomainModel()