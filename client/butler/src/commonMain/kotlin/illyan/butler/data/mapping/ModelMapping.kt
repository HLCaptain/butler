package illyan.butler.data.mapping

import illyan.butler.data.network.model.ModelDto
import illyan.butler.db.Model
import illyan.butler.domain.model.DomainModel

fun Model.toDomainModel() = DomainModel(
    uuid = uuid,
    name = name,
    type = type,
    description = description,
    greetingMessage = greetingMessage,
    author = author
)

fun DomainModel.toNetworkModel() = ModelDto(
    uuid = uuid,
    name = name,
    type = type,
    description = description,
    greetingMessage = greetingMessage,
    author = author
)

fun ModelDto.toLocalModel() = Model(
    uuid = uuid,
    name = name,
    type = type,
    description = description,
    greetingMessage = greetingMessage,
    author = author
)

fun Model.toNetworkModel() = toDomainModel().toNetworkModel()
fun DomainModel.toLocalModel() = toNetworkModel().toLocalModel()
fun ModelDto.toDomainModel() = toLocalModel().toDomainModel()