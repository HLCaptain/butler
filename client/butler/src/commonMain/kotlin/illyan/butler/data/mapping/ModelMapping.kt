package illyan.butler.data.mapping

import illyan.butler.data.firestore.model.FirestoreModel
import illyan.butler.domain.model.DomainModel

fun FirestoreModel.toDomainModel() = DomainModel(
    name = name,
    uuid = uuid,
    type = type,
    description = description,
    greetingMessage = greetingMessage,
    author = author
)