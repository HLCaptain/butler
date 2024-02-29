package illyan.butler.data.mapping

import illyan.butler.data.network.model.ModelDto
import illyan.butler.db.Model
import illyan.butler.domain.model.DomainModel
import illyan.butler.util.log.randomUUID

fun Model.toDomainModel() = DomainModel(
    uuid = uuid,
    name = name,
    type = type,
    description = description,
    greetingMessage = greetingMessage,
    author = author
)

fun DomainModel.toNetworkModel() = ModelDto(
    id = uuid,
    name = name,
    type = type,
    description = description,
    greetingMessage = greetingMessage,
    author = author
)

fun ModelDto.toLocalModel() = Model(
    uuid = id ?: randomUUID(),
    name = name,
    type = type,
    description = description,
    greetingMessage = greetingMessage,
    author = author
)

fun Model.toNetworkModel() = toDomainModel().toNetworkModel()
fun DomainModel.toLocalModel() = toNetworkModel().toLocalModel()
fun ModelDto.toDomainModel() = toLocalModel().toDomainModel()