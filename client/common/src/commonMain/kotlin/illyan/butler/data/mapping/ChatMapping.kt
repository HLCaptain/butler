package illyan.butler.data.mapping

import illyan.butler.data.firestore.model.FirestoreChat
import illyan.butler.db.Chat
import illyan.butler.domain.model.DomainChat

fun Chat.toDomainModel() = DomainChat(
    uuid = uuid,
    name = name,
    userUUID = userUUID,
    modelUUID = modelUUID,
    messages = messages
)

fun DomainChat.toNetworkModel() = FirestoreChat(
    uuid = uuid,
    name = name,
    userUUID = userUUID,
    modelUUID = modelUUID,
    messages = messages
)

fun FirestoreChat.toLocalModel() = Chat(
    uuid = uuid,
    name = name,
    userUUID = userUUID,
    modelUUID = modelUUID,
    messages = messages
)

fun Chat.toNetworkModel() = toDomainModel().toNetworkModel()
fun DomainChat.toLocalModel() = toNetworkModel().toLocalModel()
fun FirestoreChat.toDomainModel() = toLocalModel().toDomainModel()