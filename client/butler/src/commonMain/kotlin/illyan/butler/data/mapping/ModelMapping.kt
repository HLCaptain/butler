package illyan.butler.data.mapping

import illyan.butler.data.network.model.ModelDto
import illyan.butler.db.Model
import illyan.butler.domain.model.DomainModel
import illyan.butler.util.log.randomUUID

fun Model.toDomainModel() = DomainModel(
    id = id,
    name = name,
    type = type,
    description = description,
    greetingMessage = greetingMessage,
    author = author
)

fun DomainModel.toNetworkModel() = ModelDto(
    id = id,
    name = name,
    type = type,
    description = description,
    greetingMessage = greetingMessage,
    author = author
)

fun ModelDto.toLocalModel() = Model(
    id = id ?: randomUUID(),
    name = name,
    type = type,
    description = description,
    greetingMessage = greetingMessage,
    author = author
)

fun Model.toNetworkModel() = toDomainModel().toNetworkModel()
fun DomainModel.toLocalModel() = toNetworkModel().toLocalModel()
fun ModelDto.toDomainModel() = toLocalModel().toDomainModel()